package com.gustavosiqueira.payment.fraud.application.use_case;

public interface UseCase<I> {

    void execute(I input);
}