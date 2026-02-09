package com.gustavosiqueira.payment.transaction.application.use_case;

import com.gustavosiqueira.payment.transaction.application.event.TransactionFinallyEvent;
import com.gustavosiqueira.payment.transaction.application.exception.TransactionNotFound;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionEventPublisher;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionFinallyEventPublisher;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionRepository;
import com.gustavosiqueira.payment.transaction.domain.Transaction;
import com.gustavosiqueira.payment.transaction.domain.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.gustavosiqueira.payment.transaction.domain.FraudDecision.APPROVED;
import static com.gustavosiqueira.payment.transaction.domain.WalletEventType.BALANCE_DEBITED;

@Service
@RequiredArgsConstructor
public class FinallyTransactionUseCase implements UseCase<TransactionFinallyEvent> {

    private final TransactionRepository transactionRepository;
    private final TransactionFinallyEventPublisher transactionFinallyEventPublisher;

    @Override
    public void execute(TransactionFinallyEvent input) throws Exception {
        var transaction = transactionRepository.findById(input.transactionId())
                .orElseThrow(TransactionNotFound::new);

        var transactionStatus = input.status().equals(BALANCE_DEBITED.name()) ? TransactionStatus.CONFIRMED :  TransactionStatus.CANCELED;

        var transactionUpdated = new Transaction(
                transaction.getId(),
                transaction.getFromWalletId(),
                transaction.getToWalletId(),
                transaction.getAmount(),
                transactionStatus,
                transaction.getCreatedAt()
        );

        transactionRepository.save(transactionUpdated);
        transactionFinallyEventPublisher.sendTransactionFinally();
    }
}
