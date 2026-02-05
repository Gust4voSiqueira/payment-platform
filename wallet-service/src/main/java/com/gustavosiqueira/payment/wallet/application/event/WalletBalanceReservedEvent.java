package com.gustavosiqueira.payment.wallet.application.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletBalanceReservedEvent(
        UUID transactionId,
        UUID correlationId,
        UUID userId,
        BigDecimal reservedAmount,
        String currency,
        BigDecimal availableBalanceAfter,
        BigDecimal reservedBalanceAfter,
        Instant occurredAt
) {}
