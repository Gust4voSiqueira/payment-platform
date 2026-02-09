package com.gustavosiqueira.payment.transaction.application.use_case;

import com.gustavosiqueira.payment.transaction.application.event.TransactionFinallyEvent;
import com.gustavosiqueira.payment.transaction.application.exception.TransactionNotFound;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionFinallyEventPublisher;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionRepository;
import com.gustavosiqueira.payment.transaction.domain.Transaction;
import com.gustavosiqueira.payment.transaction.domain.TransactionStatus;
import com.gustavosiqueira.payment.transaction.domain.WalletEventType;
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
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FinallyTransactionUseCaseTest {

    @InjectMocks
    private FinallyTransactionUseCase finallyTransactionUseCase;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionFinallyEventPublisher transactionFinallyEventPublisher;

    @Test
    @DisplayName("Deve confirmar transação quando saldo foi debitado")
    void shouldConfirmTransactionWhenBalanceIsDebited() throws Exception {
        var transactionId = UUID.randomUUID();
        var transaction = buildTransaction(transactionId);
        var event = buildEvent(transactionId, WalletEventType.BALANCE_DEBITED.name());

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        finallyTransactionUseCase.execute(event);

        var savedTransaction = captureTransaction();

        assertThat(savedTransaction.getId()).isEqualTo(transactionId);
        assertThat(savedTransaction.getStatus()).isEqualTo(TransactionStatus.CONFIRMED);
        verify(transactionFinallyEventPublisher).sendTransactionFinally();
    }

    @Test
    @DisplayName("Deve cancelar transação quando houve erro no débito")
    void shouldCancelTransactionWhenDebitFails() throws Exception {
        var transactionId = UUID.randomUUID();
        var transaction = buildTransaction(transactionId);
        var event = buildEvent(transactionId, "OTHER_STATUS");

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        finallyTransactionUseCase.execute(event);

        var savedTransaction = captureTransaction();

        assertThat(savedTransaction.getId()).isEqualTo(transactionId);
        assertThat(savedTransaction.getStatus()).isEqualTo(TransactionStatus.CANCELED);
        verify(transactionFinallyEventPublisher).sendTransactionFinally();
    }

    @Test
    @DisplayName("Deve lançar TransactionNotFound quando transação não existir")
    void shouldThrowTransactionNotFoundWhenTransactionDoesNotExist() {
        var transactionId = UUID.randomUUID();
        var event = buildEvent(transactionId, WalletEventType.BALANCE_DEBITED.name());

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> finallyTransactionUseCase.execute(event))
                .isInstanceOf(TransactionNotFound.class);

        verifyNoInteractions(transactionFinallyEventPublisher);
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

    private TransactionFinallyEvent buildEvent(UUID transactionId, String status) {
        return new TransactionFinallyEvent(
                transactionId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                status
        );
    }

    private Transaction captureTransaction() {
        var captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        return captor.getValue();
    }
}

