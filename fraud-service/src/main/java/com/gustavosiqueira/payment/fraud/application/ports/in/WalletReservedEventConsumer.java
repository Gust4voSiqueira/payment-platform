package com.gustavosiqueira.payment.fraud.application.ports.in;

import com.gustavosiqueira.payment.fraud.application.event.WalletBalanceReservedEvent;

import java.util.function.Consumer;

public interface WalletReservedEventConsumer {

    Consumer<WalletBalanceReservedEvent> walletReserved();
}
