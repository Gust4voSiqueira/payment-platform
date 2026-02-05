package com.gustavosiqueira.payment.fraud.adapters.out.messaging;

import com.gustavosiqueira.payment.fraud.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.fraud.application.ports.out.FraudDecisionEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaFraudDecisionEventPublisher implements FraudDecisionEventPublisher {

    private final StreamBridge streamBridge;

    private static final Integer EVENT_VERSION = 1;
    private static final String SERVICE_NAME = "fraud-service";
    private static final String BINDING_NAME = "fraudDecision-out-0";

    @Override
    public void fraudDecision(FraudDecisionEvent fraudDecisionEvent) {
        var message = MessageBuilder.withPayload(fraudDecisionEvent)
                .setHeader("event_type", fraudDecisionEvent.decision())
                .setHeader("event_version", EVENT_VERSION)
                .setHeader("transaction_id", fraudDecisionEvent.transactionId())
                .setHeader("correlation_id", fraudDecisionEvent.correlationId())
                .setHeader("producer", SERVICE_NAME)
                .build();

        streamBridge.send(BINDING_NAME, message);
    }
}
