package com.gustavosiqueira.payment.transaction.application.event;

import java.util.UUID;

public record TransactionFinallyEvent(
        UUID transactionId,
        UUID correlationId,
        UUID userFromId,
        UUID userToId,
        String status
) {}
