package com.gustavosiqueira.payment.transaction.application.use_case;

import com.gustavosiqueira.payment.transaction.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.transaction.application.exception.TransactionNotFound;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionEventPublisher;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionRepository;
import com.gustavosiqueira.payment.transaction.domain.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.gustavosiqueira.payment.transaction.application.event.TransactionCreatedEvent.fromTransaction;
import static com.gustavosiqueira.payment.transaction.domain.FraudDecision.APPROVED;

@Service
@RequiredArgsConstructor
public class ApplyFraudDecisionUseCase implements UseCase<FraudDecisionEvent> {

    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher transactionEventPublisher;

    @Override
    public void execute(FraudDecisionEvent input) throws Exception {
        var transaction = transactionRepository.findById(input.transactionId())
                .orElseThrow(TransactionNotFound::new);

        var transactionStatus = input.decision().equals(APPROVED.name()) ? TransactionStatus.CONFIRMED :  TransactionStatus.CANCELED;

        var transactionCreatedEvent = fromTransaction(transaction, UUID.randomUUID(), 1);

        transactionEventPublisher.sendTransaction(transactionCreatedEvent, transactionStatus.name());
    }
}
