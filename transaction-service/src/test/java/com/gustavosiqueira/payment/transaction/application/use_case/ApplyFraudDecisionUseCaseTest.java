package com.gustavosiqueira.payment.transaction.application.use_case;

import com.gustavosiqueira.payment.transaction.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.transaction.application.exception.TransactionNotFound;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionEventPublisher;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionRepository;
import com.gustavosiqueira.payment.transaction.domain.FraudDecision;
import com.gustavosiqueira.payment.transaction.domain.Transaction;
import com.gustavosiqueira.payment.transaction.domain.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ApplyFraudDecisionUseCaseTest {

    @InjectMocks
    private ApplyFraudDecisionUseCase applyFraudDecisionUseCase;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEventPublisher transactionEventPublisher;

    @Test
    @DisplayName("Deve confirmar transação quando decisão for aprovada")
    void shouldConfirmTransactionWhenDecisionIsApproved() throws Exception {
        var transactionId = UUID.randomUUID();
        var transaction = buildTransaction(transactionId);
        var event = buildEvent(transactionId, FraudDecision.APPROVED.name());

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        applyFraudDecisionUseCase.execute(event);

        verify(transactionEventPublisher).sendTransaction(any(), eq(TransactionStatus.CONFIRMED.name()));
    }

    @Test
    @DisplayName("Deve cancelar transação quando decisão for rejeitada")
    void shouldCancelTransactionWhenDecisionIsRejected() throws Exception {
        var transactionId = UUID.randomUUID();
        var transaction = buildTransaction(transactionId);
        var event = buildEvent(transactionId, FraudDecision.REJECTED.name());

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        applyFraudDecisionUseCase.execute(event);

        verify(transactionEventPublisher).sendTransaction(any(), eq(TransactionStatus.CANCELED.name()));
    }

    @Test
    @DisplayName("Deve lançar TransactionNotFound quando transação não existir")
    void shouldThrowTransactionNotFoundWhenTransactionDoesNotExist() {
        var transactionId = UUID.randomUUID();
        var event = buildEvent(transactionId, FraudDecision.APPROVED.name());

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> applyFraudDecisionUseCase.execute(event))
                .isInstanceOf(TransactionNotFound.class);

        verifyNoInteractions(transactionEventPublisher);
    }

    private Transaction buildTransaction(UUID transactionId) {
        return new Transaction(
                transactionId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100),
                TransactionStatus.CREATED,
                Instant.now()
        );
    }

    private FraudDecisionEvent buildEvent(UUID transactionId, String decision) {
        return new FraudDecisionEvent(
                transactionId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100),
                0,
                decision,
                Instant.now()
        );
    }
}

