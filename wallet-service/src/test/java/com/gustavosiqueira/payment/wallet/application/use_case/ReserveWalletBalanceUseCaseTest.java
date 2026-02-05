package com.gustavosiqueira.payment.wallet.application.use_case;

import com.gustavosiqueira.payment.wallet.application.event.TransactionCreatedEvent;
import com.gustavosiqueira.payment.wallet.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.wallet.application.exceptions.UserNotFoundException;
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

import static com.gustavosiqueira.payment.wallet.domain.WalletEventType.BALANCE_RESERVED;
import static com.gustavosiqueira.payment.wallet.domain.WalletEventType.INSUFFICIENT_BALANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReserveWalletBalanceUseCaseTest {

    @InjectMocks
    ReserveWalletBalanceUseCase reserveWalletBalanceUseCase;

    @Mock
    WalletsRepository walletsRepository;

    @Mock
    WalletReservationsRepository walletReservationsRepository;

    @Mock
    WalletEventPublisher walletEventPublisher;

    @Test
    @DisplayName("Deve reservar saldo quando houver saldo suficiente")
    void shouldReserveBalanceWhenSufficientBalance() throws Exception {
        var walletId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var wallet = Wallet.from(
                walletId,
                userId,
                BigDecimal.valueOf(100),
                BigDecimal.ZERO
        );

        var event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                userId,
                BigDecimal.valueOf(50),
                UUID.randomUUID(),
                Instant.now(),
                1
        );

        when(walletsRepository.findWalletsByUserId(userId))
                .thenReturn(Optional.of(wallet));

        reserveWalletBalanceUseCase.execute(event);

        var walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        var reservationCaptor = ArgumentCaptor.forClass(WalletReservation.class);
        var eventCaptor = ArgumentCaptor.forClass(WalletBalanceReservedEvent.class);

        verify(walletsRepository).save(walletCaptor.capture());
        verify(walletReservationsRepository).save(reservationCaptor.capture());
        verify(walletEventPublisher).walletCreated(eventCaptor.capture(), eq(BALANCE_RESERVED.name()));

        var savedWallet = walletCaptor.getValue();
        var reservation = reservationCaptor.getValue();
        var publishedEvent = eventCaptor.getValue();

        assertThat(savedWallet.getAvailableBalance()).isEqualByComparingTo("50");
        assertThat(savedWallet.getReservedBalance()).isEqualByComparingTo("50");

        assertThat(reservation.getStatus()).isEqualTo(WalletReservationStatus.RESERVED);
        assertThat(reservation.getAmount()).isEqualByComparingTo("50");

        assertThat(publishedEvent.availableBalanceAfter()).isEqualByComparingTo("50");
        assertThat(publishedEvent.reservedBalanceAfter()).isEqualByComparingTo("50");
    }

    @Test
    @DisplayName("Deve criar reserva com status INSUFFICIENT_BALANCE quando saldo for insuficiente")
    void shouldCreateInsufficientBalanceReservationWhenBalanceIsNotEnough() throws Exception {
        var walletId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var wallet = Wallet.from(
                walletId,
                userId,
                BigDecimal.valueOf(30),
                BigDecimal.ZERO
        );

        var event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                userId,
                BigDecimal.valueOf(50),
                UUID.randomUUID(),
                Instant.now(),
                1
        );

        when(walletsRepository.findWalletsByUserId(userId))
                .thenReturn(Optional.of(wallet));

        reserveWalletBalanceUseCase.execute(event);

        var reservationCaptor = ArgumentCaptor.forClass(WalletReservation.class);
        var eventCaptor = ArgumentCaptor.forClass(WalletBalanceReservedEvent.class);

        verify(walletsRepository, never()).save(any());
        verify(walletReservationsRepository).save(reservationCaptor.capture());
        verify(walletEventPublisher).walletCreated(eventCaptor.capture(), eq(INSUFFICIENT_BALANCE.name()));

        var reservation = reservationCaptor.getValue();
        var publishedEvent = eventCaptor.getValue();

        assertThat(reservation.getStatus()).isEqualTo(WalletReservationStatus.INSUFFICIENT_BALANCE);
        assertThat(publishedEvent.availableBalanceAfter()).isEqualByComparingTo("30");
        assertThat(publishedEvent.reservedBalanceAfter()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException quando carteira não existir")
    void shouldThrowUserNotFoundExceptionWhenWalletDoesNotExist() {
        var event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(10),
                UUID.randomUUID(),
                Instant.now(),
                1
        );

        when(walletsRepository.findWalletsByUserId(event.getToWalletId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveWalletBalanceUseCase.execute(event))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(walletReservationsRepository);
        verifyNoInteractions(walletEventPublisher);
    }
}