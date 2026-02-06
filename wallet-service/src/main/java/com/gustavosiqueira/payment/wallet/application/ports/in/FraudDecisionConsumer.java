package com.gustavosiqueira.payment.wallet.application.ports.in;

import com.gustavosiqueira.payment.wallet.application.event.FraudDecisionEvent;

import java.util.function.Consumer;

public interface FraudDecisionConsumer {

    Consumer<FraudDecisionEvent> fraudDecision();
}
