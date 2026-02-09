package com.gustavosiqueira.payment.wallet.application.use_case;

import com.gustavosiqueira.payment.wallet.application.event.TransactionCreatedEvent;
import com.gustavosiqueira.payment.wallet.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.wallet.application.exceptions.UserNotFoundException;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletEventPublisher;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletReservationsRepository;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletsRepository;

import com.gustavosiqueira.payment.wallet.domain.WalletReservationStatus;
import com.gustavosiqueira.payment.wallet.domain.WalletReservation;
import com.gustavosiqueira.payment.wallet.domain.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static com.gustavosiqueira.payment.wallet.domain.Wallet.from;
import static com.gustavosiqueira.payment.wallet.domain.WalletEventType.BALANCE_RESERVED;
import static com.gustavosiqueira.payment.wallet.domain.WalletEventType.INSUFFICIENT_BALANCE;
import static com.gustavosiqueira.payment.wallet.domain.WalletReservation.from;

@Service
@RequiredArgsConstructor
public class ReserveWalletBalanceUseCase implements UseCase<TransactionCreatedEvent> {

    private final WalletsRepository walletsRepository;
    private final WalletEventPublisher walletEventPublisher;
    private final WalletReservationsRepository walletReservationsRepository;

    public static final String CURRENCY_DEFAULT = "BRL";

    @Override
    public void execute(TransactionCreatedEvent input) throws Exception {
        var wallet = walletsRepository.findWalletsByUserId(input.fromWalletId())
                .orElseThrow(UserNotFoundException::new);

        var availableAfter = wallet.getAvailableBalance().subtract(input.amount());
        var hasSufficientBalance = availableAfter.compareTo(BigDecimal.ZERO) >= 0;

        WalletReservation reservation;
        Wallet updatedWallet = wallet;

        if (hasSufficientBalance) {
            updatedWallet = from(
                    wallet.getId(),
                    wallet.getUserId(),
                    availableAfter,
                    wallet.getReservedBalance().add(input.amount())
            );

            walletsRepository.save(updatedWallet);

            reservation = from(
                    UUID.randomUUID(),
                    input.transactionId(),
                    input.amount(),
                    WalletReservationStatus.RESERVED,
                    Instant.now()
            );
        } else {
            reservation = from(
                    UUID.randomUUID(),
                    input.transactionId(),
                    input.amount(),
                    WalletReservationStatus.INSUFFICIENT_BALANCE,
                    Instant.now()
            );
        }

        walletReservationsRepository.save(reservation);

        walletEventPublisher.sendWallet(
                new WalletBalanceReservedEvent(
                        input.transactionId(),
                        input.correlationId(),
                        input.fromWalletId(),
                        input.toWalletId(),
                        input.amount(),
                        CURRENCY_DEFAULT,
                        updatedWallet.getAvailableBalance(),
                        updatedWallet.getReservedBalance(),
                        Instant.now()
                ),
                hasSufficientBalance
                        ? BALANCE_RESERVED.name()
                        : INSUFFICIENT_BALANCE.name()
        );
    }
}
