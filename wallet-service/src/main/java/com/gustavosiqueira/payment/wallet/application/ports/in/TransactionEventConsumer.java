package com.gustavosiqueira.payment.wallet.application.ports.in;

import com.gustavosiqueira.payment.wallet.application.event.TransactionCreatedEvent;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

public interface TransactionEventConsumer {

    Consumer<Message<TransactionCreatedEvent>> consumerTransaction();
}
