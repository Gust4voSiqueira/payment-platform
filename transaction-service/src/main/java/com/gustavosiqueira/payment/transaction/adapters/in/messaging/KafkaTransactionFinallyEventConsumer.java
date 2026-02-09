package com.gustavosiqueira.payment.transaction.adapters.in.messaging;

import com.gustavosiqueira.payment.transaction.application.event.TransactionFinallyEvent;
import com.gustavosiqueira.payment.transaction.application.ports.in.TransactionFinallyEventConsumer;
import com.gustavosiqueira.payment.transaction.application.use_case.FinallyTransactionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static com.gustavosiqueira.payment.transaction.domain.WalletEventType.BALANCE_CANCELED;
import static com.gustavosiqueira.payment.transaction.domain.WalletEventType.BALANCE_DEBITED;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionFinallyEventConsumer implements TransactionFinallyEventConsumer {

    private final FinallyTransactionUseCase finallyTransactionUseCase;

    public static final String EVENT_TYPE_HEADER = "event_type";

    @Bean
    @Override
    public Consumer<Message<TransactionFinallyEvent>> transactionFinally() {
        return message -> {
            var event = message.getPayload();
            var eventTypeHeader = message.getHeaders().get(EVENT_TYPE_HEADER, String.class);

            if(BALANCE_DEBITED.name().equals(eventTypeHeader) || BALANCE_CANCELED.name().equals(eventTypeHeader)) {
                log.info("[KafkaTransactionFinallyEventConsumer.transactionFinally] Event transactionFinally received | transactionId={} | status: {}",
                        event.transactionId(),
                        event.status()
                );
                try {
                     var transactionFinallyEvent = new TransactionFinallyEvent(
                             event.transactionId(),
                             event.correlationId(),
                             event.userFromId(),
                             event.userToId(),
                             eventTypeHeader
                     );
                    finallyTransactionUseCase.execute(transactionFinallyEvent);
                } catch (Exception e) {
                    log.error("ERRO!");
                }
            }
        };
    }
}
