package com.gustavosiqueira.payment.transaction.application.use_case;

import com.gustavosiqueira.payment.transaction.adapters.in.controller.dto.CreateTransactionRequest;
import com.gustavosiqueira.payment.transaction.application.event.TransactionCreatedEvent;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionEventPublisher;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionRepository;
import com.gustavosiqueira.payment.transaction.domain.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateTransactionUseCaseTest {

    @InjectMocks
    CreateTransactionUseCase createTransactionUseCase;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    TransactionEventPublisher transactionEventPublisher;

    @Test
    @DisplayName("Deve salvar a transação e publicar evento de transação criada")
    void shouldSaveTransactionAndPublishTransactionCreatedEvent() {
        var request = mock(CreateTransactionRequest.class);

        var transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        var eventCaptor = ArgumentCaptor.forClass(TransactionCreatedEvent.class);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        createTransactionUseCase.execute(request);

        verify(transactionRepository)
                .save(transactionCaptor.capture());

        verify(transactionEventPublisher)
                .transactionCreated(eventCaptor.capture());

        var savedTransaction = transactionCaptor.getValue();
        var publishedEvent = eventCaptor.getValue();

        assertThat(savedTransaction).isNotNull();
        assertEquals(savedTransaction.getId().toString(), publishedEvent.getTransactionId().toString());
        assertEquals(1, publishedEvent.getEventVersion());
        assertThat(publishedEvent.getCorrelationId())
                .isNotNull();
        assertThat(publishedEvent.getOccurredAt())
                .isNotNull();
    }
}
