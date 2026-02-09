package com.gustavosiqueira.payment.transaction.application.ports.in;

import com.gustavosiqueira.payment.transaction.application.event.FraudDecisionEvent;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

public interface FraudDecisionConsumer {

    Consumer<Message<FraudDecisionEvent>> fraudDecision();
}
