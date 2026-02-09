package com.gustavosiqueira.payment.transaction.adapters.in.messaging;

import com.gustavosiqueira.payment.transaction.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.transaction.application.ports.in.FraudDecisionConsumer;
import com.gustavosiqueira.payment.transaction.application.use_case.ApplyFraudDecisionUseCase;
import com.gustavosiqueira.payment.transaction.application.use_case.InsufficientBalanceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static com.gustavosiqueira.payment.transaction.adapters.in.messaging.KafkaTransactionFinallyEventConsumer.EVENT_TYPE_HEADER;
import static com.gustavosiqueira.payment.transaction.domain.FraudDecision.INSUFFICIENT_BALANCE;
import static com.gustavosiqueira.payment.transaction.domain.FraudDecision.REVIEW;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaFraudDecisionConsumer implements FraudDecisionConsumer {

    private final ApplyFraudDecisionUseCase applyFraudDecisionUseCase;
    private final InsufficientBalanceUseCase insufficientBalanceUseCase;

    @Bean
    @Override
    public Consumer<Message<FraudDecisionEvent>> fraudDecision() {
        return message -> {
            var event = message.getPayload();
            var eventTypeHeader = message.getHeaders().get(EVENT_TYPE_HEADER, String.class);

            log.info(
                    "[KafkaFraudDecisionConsumer.fraudDecision] Event FraudDecision received | transactionId={} | riskScore={} | decision={} | analysedAt={}",
                    event.transactionId(),
                    event.riskScore(),
                    event.decision(),
                    event.analysedAt()
            );

            try {
                if(REVIEW.name().equals(eventTypeHeader)) {
                    return;
                }
                if (INSUFFICIENT_BALANCE.name().equals(eventTypeHeader)) {
                    insufficientBalanceUseCase.execute(event);
                } else {
                    applyFraudDecisionUseCase.execute(event);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
