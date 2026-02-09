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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaTransactionEventPublisherTest {

    private static final String BINDING_NAME = "sendTransaction-out-0";
    private static final String EVENT_TYPE = "TRANSACTION_RESERVED";
    private static final String SERVICE_NAME = "transaction-service";
    private static final Integer EVENT_VERSION = 1;

    @InjectMocks
    private KafkaTransactionEventPublisher publisher;

    @Mock
    private StreamBridge streamBridge;

    @Test
    @DisplayName("Deve publicar evento de transação com payload e headers obrigatórios")
    void shouldPublishTransactionEventWithRequiredHeaders() {
        var event = buildEvent();

        var messageCaptor = ArgumentCaptor.forClass(Message.class);

        publisher.sendTransaction(event, EVENT_TYPE);

        verify(streamBridge).send(eq(BINDING_NAME), messageCaptor.capture());

        var message = messageCaptor.getValue();

        assertPayload(message, event);
        assertHeaders(message, event);
    }

    private TransactionCreatedEvent buildEvent() {
        return new TransactionCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100),
                UUID.randomUUID(),
                Instant.now(),
                1
        );
    }

    private void assertPayload(Message<?> message, TransactionCreatedEvent event) {
        assertThat(message.getPayload()).isEqualTo(event);
    }

    private void assertHeaders(Message<?> message, TransactionCreatedEvent event) {
        var headers = message.getHeaders();

        assertThat(headers.get("event_type")).isEqualTo(EVENT_TYPE);
        assertThat(headers.get("event_version")).isEqualTo(EVENT_VERSION);
        assertThat(headers.get("transaction_id")).isEqualTo(event.getTransactionId());
        assertThat(headers.get("correlation_id")).isEqualTo(event.getCorrelationId());
        assertThat(headers.get("producer")).isEqualTo(SERVICE_NAME);
    }
}