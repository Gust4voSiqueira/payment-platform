package com.gustavosiqueira.payment.transaction.application.exception;

public class TransactionNotFound extends Exception {

    public TransactionNotFound() {
        super("Transaction não encontrada");
    }
}
