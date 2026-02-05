package com.gustavosiqueira.payment.fraud.adapters.in.messaging;

import com.gustavosiqueira.payment.fraud.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.fraud.application.use_case.AnalysisFraudUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaWalletReservedEventConsumerTest {

    @InjectMocks
    private KafkaWalletReservedEventConsumer consumer;

    @Mock
    private AnalysisFraudUseCase analysisFraudUseCase;

    @Test
    @DisplayName("Deve executar análise de fraude ao consumir WalletBalanceReservedEvent")
    void shouldExecuteFraudAnalysisWhenEventIsConsumed() {
        var event = new WalletBalanceReservedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("1500"),
                "BRL",
                new BigDecimal("8500"),
                new BigDecimal("1500"),
                Instant.now()
        );

        Consumer<WalletBalanceReservedEvent> walletReservedConsumer =
                consumer.walletReserved();

        walletReservedConsumer.accept(event);

        verify(analysisFraudUseCase).execute(event);
    }
}