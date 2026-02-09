package com.gustavosiqueira.payment.transaction.adapters.out.messaging;

import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionFinallyEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionFinallyEventPublisher implements TransactionFinallyEventPublisher {

    private final StreamBridge streamBridge;

    @Override
    public void sendTransactionFinally() {
        log.info("Enviando notificação");
    }
}
