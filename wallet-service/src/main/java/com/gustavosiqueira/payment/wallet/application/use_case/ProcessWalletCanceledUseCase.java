package com.gustavosiqueira.payment.wallet.application.use_case;

import com.gustavosiqueira.payment.wallet.application.event.TransactionCreatedEvent;
import com.gustavosiqueira.payment.wallet.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.wallet.application.exceptions.UserNotFoundException;
import com.gustavosiqueira.payment.wallet.application.exceptions.WalletReservationNotFoundException;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletEventPublisher;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletReservationsRepository;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletsRepository;
import com.gustavosiqueira.payment.wallet.domain.WalletReservation;
import com.gustavosiqueira.payment.wallet.domain.WalletReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.gustavosiqueira.payment.wallet.application.use_case.ReserveWalletBalanceUseCase.CURRENCY_DEFAULT;
import static com.gustavosiqueira.payment.wallet.domain.Wallet.from;
import static com.gustavosiqueira.payment.wallet.domain.WalletEventType.BALANCE_CANCELED;

@Service
@RequiredArgsConstructor
public class ProcessWalletCanceledUseCase implements UseCase<TransactionCreatedEvent> {

    private final WalletsRepository walletsRepository;
    private final WalletEventPublisher walletEventPublisher;
    private final WalletReservationsRepository walletReservationsRepository;

    @Override
    public void execute(TransactionCreatedEvent input) throws Exception {
        var walletFrom = walletsRepository.findWalletsByUserId(input.fromWalletId())
                .orElseThrow(UserNotFoundException::new);
        var walletReservation = walletReservationsRepository.findAllByTransactionId(input.transactionId())
                .orElseThrow(WalletReservationNotFoundException::new);

        var walletFromUpdated = from(walletFrom.getId(), walletFrom.getUserId(), walletFrom.getAvailableBalance().add(input.amount()), walletFrom.getReservedBalance().subtract(input.amount()));
        var walletReservationUpdated = WalletReservation.from(walletReservation.getId(), input.transactionId(), input.amount(), WalletReservationStatus.CANCELED, walletReservation.getCreatedAt());

        walletsRepository.save(walletFromUpdated);
        walletReservationsRepository.save(walletReservationUpdated);

        walletEventPublisher.sendWallet(
                new WalletBalanceReservedEvent(
                        input.transactionId(),
                        input.correlationId(),
                        input.fromWalletId(),
                        input.toWalletId(),
                        input.amount(),
                        CURRENCY_DEFAULT,
                        walletFromUpdated.getAvailableBalance(),
                        walletFromUpdated.getReservedBalance(),
                        Instant.now()
                ),
                BALANCE_CANCELED.name()
        );
    }
}
