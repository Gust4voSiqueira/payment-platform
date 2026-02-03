package com.gustavosiqueira.payment.transaction.application.event;

import com.gustavosiqueira.payment.transaction.domain.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TransactionCreatedEvent {
    private UUID transactionId;
    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;

    private UUID correlationId;
    private Instant occurredAt;
    private Integer eventVersion;

    public static TransactionCreatedEvent fromTransaction(Transaction transaction, UUID correlationId, Integer eventVersion) {
        return new TransactionCreatedEvent(
                transaction.getId(),
                transaction.getFromWalletId(),
                transaction.getToWalletId(),
                transaction.getAmount(),
                correlationId,
                Instant.now(),
                eventVersion
        );
    }
}
