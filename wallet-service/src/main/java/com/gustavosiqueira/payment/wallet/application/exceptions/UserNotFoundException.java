package com.gustavosiqueira.payment.wallet.application.exceptions;

public class UserNotFoundException extends Exception {

    public UserNotFoundException() {
        super("Usuário não encontrado");
    }
}