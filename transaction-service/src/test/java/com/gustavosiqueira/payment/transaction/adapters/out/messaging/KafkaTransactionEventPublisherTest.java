package com.gustavosiqueira.payment.transaction.adapters.out.messaging;

import com.gustavosiqueira.payment.transaction.application.event.TransactionCreatedEvent;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaTransactionEventPublisherTest {

    @InjectMocks
    KafkaTransactionEventPublisher kafkaTransactionEventPublisher;

    @Mock
    StreamBridge streamBridge;

    @Test
    @DisplayName("Deve publicar evento de transação criada com headers obrigatórios")
    void shouldPublishTransactionCreatedEventWithRequiredHeaders() {
        var transactionId = UUID.randomUUID();
        var correlationId = UUID.randomUUID();

        var event = new TransactionCreatedEvent(
                transactionId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100),
                correlationId,
                Instant.now(),
                1
        );

        var messageCaptor = ArgumentCaptor.forClass(Message.class);

        kafkaTransactionEventPublisher.transactionCreated(event);

        verify(streamBridge)
                .send(
                        org.mockito.ArgumentMatchers.eq("transactionCreated-out-0"),
                        messageCaptor.capture()
                );

        var message = messageCaptor.getValue();

        assertThat(message.getPayload()).isEqualTo(event);

        var headers = message.getHeaders();

        assertThat(headers.get("event_type")).isEqualTo("TransactionReserved");
        assertThat(headers.get("event_version")).isEqualTo(1);
        assertThat(headers.get("transaction_id")).isEqualTo(transactionId);
        assertThat(headers.get("correlation_id")).isEqualTo(correlationId);
        assertThat(headers.get("producer")).isEqualTo("transaction-service");
    }
}
