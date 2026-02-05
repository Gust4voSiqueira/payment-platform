package com.gustavosiqueira.payment.wallet.adapters.in.messaging;

import com.gustavosiqueira.payment.wallet.application.event.TransactionCreatedEvent;
import com.gustavosiqueira.payment.wallet.application.use_case.ReserveWalletBalanceUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaTransactionEventConsumerTest {

    @InjectMocks
    KafkaTransactionEventConsumer kafkaTransactionEventConsumer;

    @Mock
    ReserveWalletBalanceUseCase reserveWalletBalanceUseCase;

    @Test
    @DisplayName("Deve consumir evento TransactionCreated e executar o use case")
    void shouldConsumeTransactionCreatedEventAndExecuteUseCase() throws Exception {
        var event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100),
                UUID.randomUUID(),
                Instant.now(),
                1
        );

        var consumer = kafkaTransactionEventConsumer.transactionCreated();

        consumer.accept(event);

        verify(reserveWalletBalanceUseCase, times(1))
                .execute(event);
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o use case falhar")
    void shouldThrowRuntimeExceptionWhenUseCaseFails() throws Exception {
        var event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100),
                UUID.randomUUID(),
                Instant.now(),
                1
        );

        doThrow(new RuntimeException("Erro ao reservar saldo"))
                .when(reserveWalletBalanceUseCase)
                .execute(event);

        var consumer = kafkaTransactionEventConsumer.transactionCreated();

        assertThatThrownBy(() -> consumer.accept(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao reservar saldo");

        verify(reserveWalletBalanceUseCase, times(1))
                .execute(event);
    }
}