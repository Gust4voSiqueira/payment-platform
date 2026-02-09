package com.gustavosiqueira.payment.transaction.application.use_case;

import com.gustavosiqueira.payment.transaction.adapters.in.controller.dto.CreateTransactionRequest;
import com.gustavosiqueira.payment.transaction.application.event.TransactionCreatedEvent;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionEventPublisher;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionRepository;
import com.gustavosiqueira.payment.transaction.domain.Transaction;
import com.gustavosiqueira.payment.transaction.domain.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateTransactionUseCaseTest {

    @InjectMocks
    private CreateTransactionUseCase createTransactionUseCase;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEventPublisher transactionEventPublisher;

    @Test
    @DisplayName("Deve salvar a transação e publicar evento de transação criada")
    void shouldSaveTransactionAndPublishTransactionCreatedEvent() {
        var request = buildRequest();

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        createTransactionUseCase.execute(request);

        var savedTransaction = captureTransaction();
        var publishedEvent = captureEvent();

        assertThat(savedTransaction).isNotNull();
        assertThat(savedTransaction.getId().toString())
                .isEqualTo(publishedEvent.getTransactionId().toString());
        assertThat(publishedEvent.getEventVersion()).isEqualTo(1);
        assertThat(publishedEvent.getCorrelationId()).isNotNull();
        assertThat(publishedEvent.getOccurredAt()).isNotNull();
    }

    private CreateTransactionRequest buildRequest() {
        return new CreateTransactionRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100)
        );
    }

    private Transaction captureTransaction() {
        var captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        return captor.getValue();
    }

    private TransactionCreatedEvent captureEvent() {
        var captor = ArgumentCaptor.forClass(TransactionCreatedEvent.class);
        verify(transactionEventPublisher).sendTransaction(captor.capture(), eq(TransactionStatus.CREATED.name()));
        return captor.getValue();
    }
}
