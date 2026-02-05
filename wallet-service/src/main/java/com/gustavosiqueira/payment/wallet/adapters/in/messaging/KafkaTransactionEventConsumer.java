package com.gustavosiqueira.payment.wallet.adapters.in.messaging;

import com.gustavosiqueira.payment.wallet.application.event.TransactionCreatedEvent;
import com.gustavosiqueira.payment.wallet.application.ports.in.TransactionEventConsumer;
import com.gustavosiqueira.payment.wallet.application.use_case.ReserveWalletBalanceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionEventConsumer implements TransactionEventConsumer {

    private final ReserveWalletBalanceUseCase reserveWalletBalanceUseCase;

    @Bean
    @Override
    public Consumer<TransactionCreatedEvent> transactionCreated() {
        return event -> {
            log.info(
                    "[KafkaTransactionEventConsumer.transactionCreated] Event TransactionCreated received | transactionId={} | fromWalletId={} | toWalletId={} | amount={}",
                    event.getTransactionId(),
                    event.getFromWalletId(),
                    event.getToWalletId(),
                    event.getAmount()
            );

            try {
                reserveWalletBalanceUseCase.execute(event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
