package com.gustavosiqueira.payment.wallet.application.ports.in;

import com.gustavosiqueira.payment.wallet.application.event.TransactionCreatedEvent;

import java.util.function.Consumer;

public interface TransactionEventConsumer {

    Consumer<TransactionCreatedEvent> transactionCreated();
}
