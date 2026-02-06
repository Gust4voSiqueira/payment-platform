package com.gustavosiqueira.payment.fraud.application.ports.in;

import com.gustavosiqueira.payment.fraud.application.event.WalletBalanceReservedEvent;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

public interface WalletReservedEventConsumer {

    Consumer<Message<WalletBalanceReservedEvent>> walletReserved();
}
