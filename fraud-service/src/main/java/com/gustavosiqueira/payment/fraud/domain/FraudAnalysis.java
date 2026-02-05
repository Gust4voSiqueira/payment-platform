package com.gustavosiqueira.payment.fraud.domain;

import com.gustavosiqueira.payment.fraud.application.event.WalletBalanceReservedEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "fraud_analysis",
        indexes = {
                @Index(name = "idx_fraud_user", columnList = "user_id"),
                @Index(name = "idx_fraud_transaction", columnList = "transaction_id"),
                @Index(name = "idx_fraud_analyzed_at", columnList = "analyzed_at")
        }
)
public class FraudAnalysis {

    @Id
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fraud_decision", nullable = false)
    private FraudDecision fraudDecision;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "reason")
    @Enumerated(EnumType.STRING)
    private FraudReason reason;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;

    public static FraudAnalysis fromWalletBalanceReservedEvent(WalletBalanceReservedEvent walletBalanceReservedEvent, FraudDecision fraudDecision, FraudReason reason) {
        return FraudAnalysis.builder()
                .id(UUID.randomUUID())
                .transactionId(walletBalanceReservedEvent.transactionId())
                .userId(walletBalanceReservedEvent.userId())
                .fraudDecision(fraudDecision)
                .amount(walletBalanceReservedEvent.reservedAmount())
                .reason(reason)
                .analyzedAt(Instant.now())
                .build();
    }
}
