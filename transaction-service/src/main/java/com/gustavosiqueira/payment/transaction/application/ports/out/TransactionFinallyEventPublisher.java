package com.gustavosiqueira.payment.transaction.application.ports.out;

public interface TransactionFinallyEventPublisher {

    void sendTransactionFinally();
}
