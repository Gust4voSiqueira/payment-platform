package com.gustavosiqueira.payment.wallet.adapters.out.messaging;

import com.gustavosiqueira.payment.wallet.application.event.WalletBalanceReservedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaWalletEventPublisherTest {

    @InjectMocks
    private KafkaWalletEventPublisher kafkaWalletEventPublisher;

    @Mock
    private StreamBridge streamBridge;

    @Test
    @DisplayName("Deve publicar evento de reserva criada com headers obrigatórios")
    void shouldPublishReserveCreatedEventWithRequiredHeaders() {
        var transactionId = UUID.randomUUID();
        var correlationId = UUID.randomUUID();

        var event = new WalletBalanceReservedEvent(
                transactionId,
                correlationId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100),
                "BRL",
                BigDecimal.valueOf(900),
                BigDecimal.valueOf(100),
                Instant.now()
        );

        var eventType = "BALANCE_RESERVED";

        kafkaWalletEventPublisher.sendWallet(event, eventType);

        var messageCaptor = ArgumentCaptor.forClass(Message.class);

        verify(streamBridge).send(
                eq("walletReserved-out-0"),
                messageCaptor.capture()
        );

        var message = messageCaptor.getValue();
        assertThat(message.getPayload()).isEqualTo(event);

        assertThat(message.getHeaders())
                .containsEntry("event_type", eventType)
                .containsEntry("event_version", 1)
                .containsEntry("transaction_id", transactionId)
                .containsEntry("correlation_id", correlationId)
                .containsEntry("producer", "wallet-service");
    }

}
