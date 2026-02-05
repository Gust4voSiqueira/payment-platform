package com.gustavosiqueira.payment.wallet.application.use_case;

public interface UseCase<I> {

    void execute(I input) throws Exception;
}