package com.gustavosiqueira.payment.transaction.application.use_case;

import com.gustavosiqueira.payment.transaction.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.transaction.application.exception.TransactionNotFound;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionRepository;
import com.gustavosiqueira.payment.transaction.domain.Transaction;
import com.gustavosiqueira.payment.transaction.domain.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InsufficientBalanceUseCase implements UseCase<FraudDecisionEvent> {

    private final TransactionRepository transactionRepository;

    @Override
    public void execute(FraudDecisionEvent input) throws Exception {
        var transaction = transactionRepository.findById(input.transactionId())
                .orElseThrow(TransactionNotFound::new);

        var transactionUpdated = new Transaction(
                transaction.getId(),
                transaction.getFromWalletId(),
                transaction.getToWalletId(),
                transaction.getAmount(),
                TransactionStatus.CANCELED,
                transaction.getCreatedAt()
        );

        transactionRepository.save(transactionUpdated);
    }
}
