package com.gustavosiqueira.payment.wallet.adapters.out.messaging;

import com.gustavosiqueira.payment.wallet.application.event.WalletCreatedEvent;
import com.gustavosiqueira.payment.wallet.application.ports.out.WalletEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaWalletEventPublisher implements WalletEventPublisher {

    private final StreamBridge streamBridge;

    private static final Integer EVENT_VERSION = 1;
    private static final String SERVICE_NAME = "wallet-service";
    private static final String BINDING_NAME = "walletCreated-out-0";

    @Override
    public void walletCreated(WalletCreatedEvent event, String eventType) {
        var message = MessageBuilder.withPayload(event)
                .setHeader("event_type", eventType)
                .setHeader("event_version", EVENT_VERSION)
                .setHeader("transaction_id", event.transactionId())
                .setHeader("correlation_id", event.correlationId())
                .setHeader("producer", SERVICE_NAME)
                .build();

        streamBridge.send(BINDING_NAME, message);
    }
}
