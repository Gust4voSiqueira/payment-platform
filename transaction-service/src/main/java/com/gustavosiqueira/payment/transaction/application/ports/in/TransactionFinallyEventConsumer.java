package com.gustavosiqueira.payment.transaction.application.ports.in;

import com.gustavosiqueira.payment.transaction.application.event.TransactionFinallyEvent;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

public interface TransactionFinallyEventConsumer {

    Consumer<Message<TransactionFinallyEvent>> transactionFinally();
}
