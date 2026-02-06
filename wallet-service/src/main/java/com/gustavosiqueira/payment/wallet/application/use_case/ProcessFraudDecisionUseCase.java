package com.gustavosiqueira.payment.wallet.application.use_case;

import com.gustavosiqueira.payment.wallet.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.wallet.application.exceptions.UserNotFoundException;
import com.gustavosiqueira.payment.wallet.application.exceptions.WalletReservationNotFoundException;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletReservationsRepository;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletsRepository;
import com.gustavosiqueira.payment.wallet.domain.WalletReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.gustavosiqueira.payment.wallet.domain.Wallet.from;
import static com.gustavosiqueira.payment.wallet.domain.WalletReservation.from;

@Service
@RequiredArgsConstructor
public class ProcessFraudDecisionUseCase implements UseCase<FraudDecisionEvent> {

    private final WalletsRepository walletsRepository;
    private final WalletReservationsRepository walletReservationsRepository;

    @Override
    public void execute(FraudDecisionEvent input) throws Exception {
        var walletFrom = walletsRepository.findWalletsByUserId(input.userFromId())
                .orElseThrow(UserNotFoundException::new);
        var walletTo = walletsRepository.findWalletsByUserId(input.userToId())
                .orElseThrow(UserNotFoundException::new);
        var walletReservation = walletReservationsRepository.findAllByTransactionId(input.transactionId())
                .orElseThrow(WalletReservationNotFoundException::new);

        var walletFromUpdated = from(walletFrom.getId(), walletFrom.getUserId(), walletFrom.getAvailableBalance(), walletFrom.getReservedBalance().subtract(input.amount()));
        var walletToUpdated = from(walletTo.getId(), walletTo.getUserId(), walletTo.getAvailableBalance().add(input.amount()), walletTo.getReservedBalance());
        var walletReservationUpdated = from(walletReservation.getId(), input.transactionId(), input.amount(), WalletReservationStatus.RELEASED, walletReservation.getCreatedAt());

        walletsRepository.save(walletToUpdated);
        walletsRepository.save(walletFromUpdated);
        walletReservationsRepository.save(walletReservationUpdated);
    }
}
