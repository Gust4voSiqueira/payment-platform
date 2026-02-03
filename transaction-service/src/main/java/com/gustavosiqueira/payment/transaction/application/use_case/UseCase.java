package com.gustavosiqueira.payment.transaction.application.use_case;

public interface UseCase<I> {

    void execute(I input);
}