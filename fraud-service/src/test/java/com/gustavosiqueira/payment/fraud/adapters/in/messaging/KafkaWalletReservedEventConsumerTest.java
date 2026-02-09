package com.gustavosiqueira.payment.fraud.adapters.in.messaging;

import com.gustavosiqueira.payment.fraud.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.fraud.application.use_case.AnalysisFraudUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.support.MessageBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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
        var payload = new WalletBalanceReservedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("1500"),
                "BRL",
                new BigDecimal("8500"),
                new BigDecimal("1500"),
                Instant.now()
        );
        var event = MessageBuilder
                .withPayload(payload)
                .copyHeaders(Map.of("event_type", "BALANCE_RESERVED"))
                .build();

        var walletReservedConsumer = consumer.walletReserved();

        walletReservedConsumer.accept(event);

        verify(analysisFraudUseCase).execute(event.getPayload());
    }
}
