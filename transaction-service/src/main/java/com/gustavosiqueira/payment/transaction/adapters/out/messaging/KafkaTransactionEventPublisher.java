package com.gustavosiqueira.payment.transaction.adapters.out.messaging;

import com.gustavosiqueira.payment.transaction.application.event.TransactionCreatedEvent;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaTransactionEventPublisher implements TransactionEventPublisher {

    private final StreamBridge streamBridge;

    private static final String BINDING_NAME = "transactionCreated-out-0";
    private static final String SERVICE_NAME = "transaction-service";
    private static final String EVENT_TYPE = "TransactionReserved";
    private static final Integer EVENT_VERSION = 1;

    @Override
    public void transactionCreated(TransactionCreatedEvent event) {
        var message = MessageBuilder.withPayload(event)
                        .setHeader("event_type", EVENT_TYPE)
                        .setHeader("event_version", EVENT_VERSION)
                        .setHeader("transaction_id", event.getTransactionId())
                        .setHeader("correlation_id", event.getCorrelationId())
                        .setHeader("producer", SERVICE_NAME)
                        .build();

        streamBridge.send(BINDING_NAME, message);
    }
}
