package com.gustavosiqueira.payment.fraud.adapters.out.messaging;

import com.gustavosiqueira.payment.fraud.application.event.FraudDecisionEvent;
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
class KafkaFraudDecisionEventPublisherTest {

    @InjectMocks
    private KafkaFraudDecisionEventPublisher publisher;

    @Mock
    private StreamBridge streamBridge;

    @Test
    @DisplayName("Deve publicar evento de decisão de fraude com headers obrigatórios")
    void shouldPublishFraudDecisionEventWithRequiredHeaders() {
        var transactionId = UUID.randomUUID();
        var correlationId = UUID.randomUUID();
        var userToId = UUID.randomUUID();
        var userFromId = UUID.randomUUID();

        var event = new FraudDecisionEvent(
                transactionId,
                correlationId,
                userToId,
                userFromId,
                BigDecimal.TEN,
                20,
                "REJECTED",
                Instant.now()
        );

        var messageCaptor = ArgumentCaptor.forClass(Message.class);

        publisher.fraudDecision(event);

        verify(streamBridge).send(
                org.mockito.ArgumentMatchers.eq("fraudDecision-out-0"),
                messageCaptor.capture()
        );

        var message = messageCaptor.getValue();

        assertThat(message.getPayload()).isEqualTo(event);
        assertThat(message.getHeaders().get("event_type")).isEqualTo(event.decision());
        assertThat(message.getHeaders().get("event_version")).isEqualTo(1);
        assertThat(message.getHeaders().get("transaction_id")).isEqualTo(transactionId);
        assertThat(message.getHeaders().get("correlation_id")).isEqualTo(correlationId);
        assertThat(message.getHeaders().get("producer")).isEqualTo("fraud-service");
    }
}