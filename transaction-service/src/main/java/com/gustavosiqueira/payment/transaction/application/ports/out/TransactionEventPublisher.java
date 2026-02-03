package com.gustavosiqueira.payment.transaction.application.ports.out;

import com.gustavosiqueira.payment.transaction.application.event.TransactionCreatedEvent;

public interface TransactionEventPublisher {

    void transactionCreated(TransactionCreatedEvent event);
}
