package com.gustavosiqueira.payment.transaction.adapters.in.messaging;

import com.gustavosiqueira.payment.transaction.application.event.TransactionFinallyEvent;
import com.gustavosiqueira.payment.transaction.application.use_case.FinallyTransactionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.UUID;
import java.util.function.Consumer;

import static com.gustavosiqueira.payment.transaction.adapters.in.messaging.KafkaTransactionFinallyEventConsumer.EVENT_TYPE_HEADER;
import static com.gustavosiqueira.payment.transaction.domain.WalletEventType.BALANCE_CANCELED;
import static com.gustavosiqueira.payment.transaction.domain.WalletEventType.BALANCE_DEBITED;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaTransactionFinallyEventConsumerTest {

    @InjectMocks
    private KafkaTransactionFinallyEventConsumer consumer;

    @Mock
    private FinallyTransactionUseCase finallyTransactionUseCase;

    @Test
    @DisplayName("Deve executar FinallyTransactionUseCase quando evento for BALANCE_DEBITED")
    void shouldExecuteFinallyUseCaseWhenBalanceDebited() throws Exception {
        var message = buildMessage(BALANCE_DEBITED.name());

        Consumer<Message<TransactionFinallyEvent>> handler = consumer.transactionFinally();
        handler.accept(message);

        verify(finallyTransactionUseCase).execute(any(TransactionFinallyEvent.class));
    }

    @Test
    @DisplayName("Deve executar FinallyTransactionUseCase quando evento for BALANCE_CANCELED")
    void shouldExecuteFinallyUseCaseWhenBalanceCanceled() throws Exception {
        var message = buildMessage(BALANCE_CANCELED.name());

        consumer.transactionFinally().accept(message);

        verify(finallyTransactionUseCase).execute(any(TransactionFinallyEvent.class));
    }

    @Test
    @DisplayName("Não deve executar use case para event_type desconhecido")
    void shouldIgnoreUnknownEventType() {
        var message = buildMessage("UNKNOWN_EVENT");

        consumer.transactionFinally().accept(message);

        verifyNoInteractions(finallyTransactionUseCase);
    }

    @Test
    @DisplayName("Não deve lançar exceção quando FinallyTransactionUseCase falhar")
    void shouldNotThrowExceptionWhenUseCaseThrows() throws Exception {
        var message = buildMessage(BALANCE_DEBITED.name());

        doThrow(new RuntimeException("boom"))
                .when(finallyTransactionUseCase)
                .execute(any(TransactionFinallyEvent.class));

        consumer.transactionFinally().accept(message);

        verify(finallyTransactionUseCase).execute(any(TransactionFinallyEvent.class));
    }

    private Message<TransactionFinallyEvent> buildMessage(String eventType) {
        return MessageBuilder
                .withPayload(buildEvent())
                .setHeader(EVENT_TYPE_HEADER, eventType)
                .build();
    }

    private TransactionFinallyEvent buildEvent() {
        return new TransactionFinallyEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ANY_STATUS"
        );
    }
}