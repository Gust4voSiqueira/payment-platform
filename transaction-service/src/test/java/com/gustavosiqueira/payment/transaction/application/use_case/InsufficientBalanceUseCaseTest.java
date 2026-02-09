package com.gustavosiqueira.payment.transaction.application.use_case;

import com.gustavosiqueira.payment.transaction.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.transaction.application.exception.TransactionNotFound;
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
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InsufficientBalanceUseCaseTest {

    @InjectMocks
    private InsufficientBalanceUseCase insufficientBalanceUseCase;

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Deve cancelar transação quando saldo insuficiente")
    void shouldCancelTransactionWhenInsufficientBalance() throws Exception {
        var transactionId = UUID.randomUUID();
        var transaction = buildTransaction(transactionId);
        var event = buildEvent(transactionId);

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        insufficientBalanceUseCase.execute(event);

        var savedTransaction = captureTransaction();

        assertThat(savedTransaction.getId()).isEqualTo(transactionId);
        assertThat(savedTransaction.getStatus()).isEqualTo(TransactionStatus.CANCELED);
        assertThat(savedTransaction.getFromWalletId()).isEqualTo(transaction.getFromWalletId());
        assertThat(savedTransaction.getToWalletId()).isEqualTo(transaction.getToWalletId());
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo(transaction.getAmount());
    }

    @Test
    @DisplayName("Deve lançar TransactionNotFound quando transação não existir")
    void shouldThrowTransactionNotFoundWhenTransactionDoesNotExist() {
        var transactionId = UUID.randomUUID();
        var event = buildEvent(transactionId);

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> insufficientBalanceUseCase.execute(event))
                .isInstanceOf(TransactionNotFound.class);
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

    private FraudDecisionEvent buildEvent(UUID transactionId) {
        return new FraudDecisionEvent(
                transactionId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100),
                0,
                "REJECTED",
                Instant.now()
        );
    }


    private Transaction captureTransaction() {
        var captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        return captor.getValue();
    }
}

