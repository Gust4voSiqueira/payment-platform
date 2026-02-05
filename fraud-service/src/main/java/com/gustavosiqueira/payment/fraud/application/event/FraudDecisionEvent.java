package com.gustavosiqueira.payment.fraud.application.event;

import java.time.Instant;
import java.util.UUID;

public record FraudDecisionEvent(
        UUID transactionId,
        UUID correlationId,
        Integer riskScore,
        String decision,
        Instant analysedAt
) {}
