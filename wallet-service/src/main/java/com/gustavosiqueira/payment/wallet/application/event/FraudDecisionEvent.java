package com.gustavosiqueira.payment.wallet.application.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FraudDecisionEvent(
        UUID transactionId,
        UUID correlationId,
        UUID userFromId,
        UUID userToId,
        BigDecimal amount,
        Integer riskScore,
        String decision,
        Instant analysedAt
) {}
