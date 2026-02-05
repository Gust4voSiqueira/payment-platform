package com.gustavosiqueira.payment.fraud.application.ports.out;

import com.gustavosiqueira.payment.fraud.application.event.FraudDecisionEvent;

public interface FraudDecisionEventPublisher {

    void fraudDecision(FraudDecisionEvent fraudDecisionEvent);
}
