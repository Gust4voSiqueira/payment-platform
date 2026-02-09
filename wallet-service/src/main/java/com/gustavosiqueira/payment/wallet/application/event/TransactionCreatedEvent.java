package com.gustavosiqueira.payment.wallet.application.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEvent(
         UUID transactionId,
         UUID fromWalletId,
         UUID toWalletId,
         BigDecimal amount,
         UUID correlationId,
         Instant occurredAt,
         Integer eventVersion
) {}
