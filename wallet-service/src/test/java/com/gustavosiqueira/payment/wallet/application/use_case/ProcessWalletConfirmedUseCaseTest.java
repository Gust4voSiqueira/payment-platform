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

import static com.gustavosiqueira.payment.wallet.domain.WalletEventType.BALANCE_DEBITED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProcessWalletConfirmedUseCaseTest {

    @InjectMocks
    private ProcessWalletConfirmedUseCase processWalletConfirmedUseCase;

    @Mock
    private WalletsRepository walletsRepository;

    @Mock
    private WalletEventPublisher walletEventPublisher;

    @Mock
    private WalletReservationsRepository walletReservationsRepository;

    @Test
    @DisplayName("Deve debitar saldo da carteira origem e creditar na carteira destino")
    void shouldDebitFromSourceAndCreditToDestinationWallet() throws Exception {
        var fromWalletId = UUID.randomUUID();
        var toWalletId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(50);

        var walletFrom = buildWallet(fromWalletId, BigDecimal.valueOf(100), BigDecimal.valueOf(50));
        var walletTo = buildWallet(toWalletId, BigDecimal.valueOf(100), BigDecimal.ZERO);
        var reservation = buildReservation(transactionId, amount);
        var event = buildEvent(fromWalletId, toWalletId, transactionId, amount);

        when(walletsRepository.findWalletsByUserId(fromWalletId))
                .thenReturn(Optional.of(walletFrom));
        when(walletsRepository.findWalletsByUserId(toWalletId))
                .thenReturn(Optional.of(walletTo));
        when(walletReservationsRepository.findAllByTransactionId(transactionId))
                .thenReturn(Optional.of(reservation));

        processWalletConfirmedUseCase.execute(event);

        var savedWallets = captureWallets();
        var savedReservation = captureReservation();

        var savedWalletTo = savedWallets.get(0);
        var savedWalletFrom = savedWallets.get(1);

        assertThat(savedWalletFrom.getAvailableBalance()).isEqualByComparingTo("100");
        assertThat(savedWalletFrom.getReservedBalance()).isEqualByComparingTo("0");

        assertThat(savedWalletTo.getAvailableBalance()).isEqualByComparingTo("150");
        assertThat(savedWalletTo.getReservedBalance()).isEqualByComparingTo("0");

        assertThat(savedReservation.getStatus()).isEqualTo(WalletReservationStatus.RELEASED);

        verify(walletEventPublisher).sendWallet(any(WalletBalanceReservedEvent.class), eq(BALANCE_DEBITED.name()));
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando carteira origem não existir")
    void shouldThrowUserNotFoundExceptionWhenSourceWalletDoesNotExist() {
        var fromWalletId = UUID.randomUUID();
        var toWalletId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(50);
        var event = buildEvent(fromWalletId, toWalletId, transactionId, amount);

        when(walletsRepository.findWalletsByUserId(fromWalletId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> processWalletConfirmedUseCase.execute(event))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(walletReservationsRepository);
        verifyNoInteractions(walletEventPublisher);
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando carteira destino não existir")
    void shouldThrowUserNotFoundExceptionWhenDestinationWalletDoesNotExist() {
        var fromWalletId = UUID.randomUUID();
        var toWalletId = UUID.randomUUID();
        var transactionId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(50);
        var walletFrom = buildWallet(fromWalletId, BigDecimal.valueOf(100), BigDecimal.valueOf(50));
        var event = buildEvent(fromWalletId, toWalletId, transactionId, amount);

        when(walletsRepository.findWalletsByUserId(fromWalletId))
                .thenReturn(Optional.of(walletFrom));
        when(walletsRepository.findWalletsByUserId(toWalletId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> processWalletConfirmedUseCase.execute(event))
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
        var walletTo = buildWallet(toWalletId, BigDecimal.valueOf(100), BigDecimal.ZERO);
        var event = buildEvent(fromWalletId, toWalletId, transactionId, amount);

        when(walletsRepository.findWalletsByUserId(fromWalletId))
                .thenReturn(Optional.of(walletFrom));
        when(walletsRepository.findWalletsByUserId(toWalletId))
                .thenReturn(Optional.of(walletTo));
        when(walletReservationsRepository.findAllByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> processWalletConfirmedUseCase.execute(event))
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

    private java.util.List<Wallet> captureWallets() {
        var captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletsRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        return captor.getAllValues();
    }

    private WalletReservation captureReservation() {
        var captor = ArgumentCaptor.forClass(WalletReservation.class);
        verify(walletReservationsRepository).save(captor.capture());
        return captor.getValue();
    }
}

