package com.gustavosiqueira.payment.wallet.application.use_case;

import com.gustavosiqueira.payment.wallet.application.event.TransactionCreatedEvent;
import com.gustavosiqueira.payment.wallet.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.wallet.application.exceptions.UserNotFoundException;
import com.gustavosiqueira.payment.wallet.application.exceptions.WalletReservationNotFoundException;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletEventPublisher;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletReservationsRepository;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletsRepository;
import com.gustavosiqueira.payment.wallet.domain.Wallet;
import com.gustavosiqueira.payment.wallet.domain.WalletReservation;
import com.gustavosiqueira.payment.wallet.domain.WalletReservationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.gustavosiqueira.payment.wallet.domain.WalletEventType.BALANCE_CANCELED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProcessWalletCanceledUseCaseTest {

    @InjectMocks
    private ProcessWalletCanceledUseCase processWalletCanceledUseCase;

    @Mock
    private WalletsRepository walletsRepository;

    @Mock
    private WalletEventPublisher walletEventPublisher;

    @Mock
    private WalletReservationsRepository walletReservationsRepository;

    @Test
    @DisplayName("Deve liberar saldo reservado quando transação for cancelada")
    void shouldReleaseReservedBalanceWhenTransactionIsCanceled() throws Exception {
        var fromWalletId = UUID.randomUUID();
        var toWalletId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(50);

        var walletFrom = buildWallet(fromWalletId, BigDecimal.valueOf(50), BigDecimal.valueOf(50));
        var reservation = buildReservation(transactionId, amount);
        var event = buildEvent(fromWalletId, toWalletId, transactionId, amount);

        when(walletsRepository.findWalletsByUserId(fromWalletId))
                .thenReturn(Optional.of(walletFrom));
        when(walletReservationsRepository.findAllByTransactionId(transactionId))
                .thenReturn(Optional.of(reservation));

        processWalletCanceledUseCase.execute(event);

        var savedWallet = captureWallet();
        var savedReservation = captureReservation();

        assertThat(savedWallet.getAvailableBalance()).isEqualByComparingTo("100");
        assertThat(savedWallet.getReservedBalance()).isEqualByComparingTo("0");

        assertThat(savedReservation.getStatus()).isEqualTo(WalletReservationStatus.CANCELED);
        assertThat(savedReservation.getAmount()).isEqualByComparingTo(amount);

        verify(walletEventPublisher).sendWallet(any(WalletBalanceReservedEvent.class), eq(BALANCE_CANCELED.name()));
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando carteira não existir")
    void shouldThrowUserNotFoundExceptionWhenWalletDoesNotExist() {
        var fromWalletId = UUID.randomUUID();
        var toWalletId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(50);
        var event = buildEvent(fromWalletId, toWalletId, transactionId, amount);

        when(walletsRepository.findWalletsByUserId(fromWalletId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> processWalletCanceledUseCase.execute(event))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(walletReservationsRepository);
        verifyNoInteractions(walletEventPublisher);
    }

    @Test
    @DisplayName("Deve lançar WalletReservationNotFoundException quando reserva não existir")
    void shouldThrowWalletReservationNotFoundExceptionWhenReservationDoesNotExist() {
        var fromWalletId = UUID.randomUUID();
        var toWalletId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(50);
        var walletFrom = buildWallet(fromWalletId, BigDecimal.valueOf(100), BigDecimal.valueOf(50));
        var event = buildEvent(fromWalletId, toWalletId, transactionId, amount);

        when(walletsRepository.findWalletsByUserId(fromWalletId))
                .thenReturn(Optional.of(walletFrom));
        when(walletReservationsRepository.findAllByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> processWalletCanceledUseCase.execute(event))
                .isInstanceOf(WalletReservationNotFoundException.class);

        verifyNoInteractions(walletEventPublisher);
    }

    private Wallet buildWallet(UUID walletId, BigDecimal availableBalance, BigDecimal reservedBalance) {
        return Wallet.from(
                walletId,
                UUID.randomUUID(),
                availableBalance,
                reservedBalance
        );
    }

    private WalletReservation buildReservation(UUID transactionId, BigDecimal amount) {
        return WalletReservation.from(
                UUID.randomUUID(),
                transactionId,
                amount,
                WalletReservationStatus.RESERVED,
                Instant.now()
        );
    }

    private TransactionCreatedEvent buildEvent(UUID fromWalletId, UUID toWalletId, UUID transactionId, BigDecimal amount) {
        return new TransactionCreatedEvent(
                transactionId,
                fromWalletId,
                toWalletId,
                amount,
                UUID.randomUUID(),
                Instant.now(),
                1
        );
    }

    private Wallet captureWallet() {
        var captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletsRepository).save(captor.capture());
        return captor.getValue();
    }

    private WalletReservation captureReservation() {
        var captor = ArgumentCaptor.forClass(WalletReservation.class);
        verify(walletReservationsRepository).save(captor.capture());
        return captor.getValue();
    }
}

