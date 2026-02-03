package com.gustavosiqueira.payment.transaction.application.use_case;

import com.gustavosiqueira.payment.transaction.adapters.in.controller.dto.CreateTransactionRequest;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionEventPublisher;
import com.gustavosiqueira.payment.transaction.application.ports.out.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.gustavosiqueira.payment.transaction.application.event.TransactionCreatedEvent.fromTransaction;
import static com.gustavosiqueira.payment.transaction.domain.Transaction.fromCreateTransactionRequest;

@Service
@RequiredArgsConstructor
public class CreateTransactionUseCase implements UseCase<CreateTransactionRequest> {

    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher transactionEventPublisher;

    private static final Integer DEFAULT_EVENT_VERSION = 1;

    @Override
    public void execute(CreateTransactionRequest input) {
        var transaction = fromCreateTransactionRequest(input);

        transactionRepository.save(transaction);

        var transactionCreatedEvent = fromTransaction(transaction, UUID.randomUUID(), DEFAULT_EVENT_VERSION);
        transactionEventPublisher.transactionCreated(transactionCreatedEvent);
    }
}
