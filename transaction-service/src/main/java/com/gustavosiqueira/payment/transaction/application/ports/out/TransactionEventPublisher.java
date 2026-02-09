package com.gustavosiqueira.payment.transaction.application.ports.out;

import com.gustavosiqueira.payment.transaction.application.event.TransactionCreatedEvent;

public interface TransactionEventPublisher {

    void sendTransaction(TransactionCreatedEvent event, String eventType);
}
