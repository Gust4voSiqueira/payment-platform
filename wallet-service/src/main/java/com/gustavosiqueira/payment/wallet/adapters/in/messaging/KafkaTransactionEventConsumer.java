package com.gustavosiqueira.payment.wallet.adapters.in.messaging;

import com.gustavosiqueira.payment.wallet.application.event.TransactionCreatedEvent;
import com.gustavosiqueira.payment.wallet.application.ports.in.TransactionEventConsumer;
import com.gustavosiqueira.payment.wallet.application.use_case.ProcessWalletCanceledUseCase;
import com.gustavosiqueira.payment.wallet.application.use_case.ProcessWalletConfirmedUseCase;
import com.gustavosiqueira.payment.wallet.application.use_case.ReserveWalletBalanceUseCase;
import com.gustavosiqueira.payment.wallet.domain.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static com.gustavosiqueira.payment.wallet.domain.TransactionStatus.CONFIRMED;
import static com.gustavosiqueira.payment.wallet.domain.TransactionStatus.CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionEventConsumer implements TransactionEventConsumer {

    private final ReserveWalletBalanceUseCase reserveWalletBalanceUseCase;
    private final ProcessWalletCanceledUseCase processWalletCanceledUseCase;
    private final ProcessWalletConfirmedUseCase processWalletConfirmedUseCase;

    private static final String EVENT_TYPE_HEADER = "event_type";

    @Bean
    @Override
    public Consumer<Message<TransactionCreatedEvent>> consumerTransaction() {
        return message -> {
            var event = message.getPayload();
            var eventTypeHeader = message.getHeaders().get(EVENT_TYPE_HEADER, String.class);

            if (eventTypeHeader == null) {
                log.error("[KafkaTransactionEventConsumer] event_type not found");
                return;
            }

            TransactionStatus status;
            try {
                status = TransactionStatus.valueOf(eventTypeHeader);
            } catch (IllegalArgumentException e) {
                log.error("[KafkaTransactionEventConsumer] invalid event_type={}", eventTypeHeader);
                return;
            }
            logEvent(status.name(), event);

            switch (status) {
                case CREATED -> {
                    try {
                        reserveWalletBalanceUseCase.execute(event);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                case CONFIRMED -> {
                    try {
                        processWalletConfirmedUseCase.execute(event);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                case CANCELED -> {
                    try {
                        processWalletCanceledUseCase.execute(event);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
    }

    private void logEvent(String eventType, TransactionCreatedEvent event) {
        log.info(
                "[KafkaTransactionEventConsumer.consumerTransaction] Event {} received | transactionId={} | fromWalletId={} | toWalletId={} | amount={}",
                eventType,
                event.transactionId(),
                event.fromWalletId(),
                event.toWalletId(),
                event.amount()
        );
    }
}