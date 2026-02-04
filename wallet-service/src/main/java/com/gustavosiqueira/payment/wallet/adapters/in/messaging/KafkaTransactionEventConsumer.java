package com.gustavosiqueira.payment.wallet.adapters.in.messaging;

import com.gustavosiqueira.payment.wallet.application.event.TransactionCreatedEvent;
import com.gustavosiqueira.payment.wallet.application.ports.in.TransactionEventConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionEventConsumer implements TransactionEventConsumer {

    @Bean
    @Override
    public Consumer<TransactionCreatedEvent> transactionCreated() {
        return event -> {
            log.info(
                    "Evento TransactionCreated recebido | transactionId={} | fromWalletId={} | toWalletId={} | amount={}",
                    event.getTransactionId(),
                    event.getFromWalletId(),
                    event.getToWalletId(),
                    event.getAmount()
            );

        };
    }
}
