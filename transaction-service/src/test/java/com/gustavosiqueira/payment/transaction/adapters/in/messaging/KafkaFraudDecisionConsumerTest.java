package com.gustavosiqueira.payment.transaction.adapters.in.messaging;

import com.gustavosiqueira.payment.transaction.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.transaction.application.use_case.ApplyFraudDecisionUseCase;
import com.gustavosiqueira.payment.transaction.application.use_case.InsufficientBalanceUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;
import java.util.UUID;

import static com.gustavosiqueira.payment.transaction.adapters.in.messaging.KafkaTransactionFinallyEventConsumer.EVENT_TYPE_HEADER;
import static com.gustavosiqueira.payment.transaction.domain.FraudDecision.INSUFFICIENT_BALANCE;
import static com.gustavosiqueira.payment.transaction.domain.FraudDecision.REVIEW;
import static com.gustavosiqueira.payment.transaction.domain.FraudDecision.APPROVED;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaFraudDecisionConsumerTest {

    @InjectMocks
    private KafkaFraudDecisionConsumer consumer;

    @Mock
    private ApplyFraudDecisionUseCase applyFraudDecisionUseCase;

    @Mock
    private InsufficientBalanceUseCase insufficientBalanceUseCase;

    @Test
    @DisplayName("Não deve executar nenhum use case quando decisão for REVIEW")
    void shouldIgnoreEventWhenDecisionIsReview() {
        var message = buildMessage(REVIEW.name());

        var handler = consumer.fraudDecision();
        handler.accept(message);

        verifyNoInteractions(applyFraudDecisionUseCase, insufficientBalanceUseCase);
    }

    @Test
    @DisplayName("Deve executar InsufficientBalanceUseCase quando decisão for INSUFFICIENT_BALANCE")
    void shouldExecuteInsufficientBalanceUseCase() throws Exception {
        var message = buildMessage(INSUFFICIENT_BALANCE.name());

        consumer.fraudDecision().accept(message);

        verify(insufficientBalanceUseCase).execute(any(FraudDecisionEvent.class));
        verifyNoInteractions(applyFraudDecisionUseCase);
    }

    @Test
    @DisplayName("Deve executar ApplyFraudDecisionUseCase quando decisão for APPROVED")
    void shouldExecuteApplyFraudDecisionUseCase() throws Exception {
        var message = buildMessage(APPROVED.name());

        consumer.fraudDecision().accept(message);

        verify(applyFraudDecisionUseCase).execute(any(FraudDecisionEvent.class));
        verifyNoInteractions(insufficientBalanceUseCase);
    }

    private Message<FraudDecisionEvent> buildMessage(String eventType) {
        return MessageBuilder
                .withPayload(buildEvent())
                .setHeader(EVENT_TYPE_HEADER, eventType)
                .build();
    }

    private FraudDecisionEvent buildEvent() {
        return new FraudDecisionEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                80,
                APPROVED.name(),
                Instant.now()
        );
    }
}