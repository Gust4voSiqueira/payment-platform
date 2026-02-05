package com.gustavosiqueira.payment.transaction.domain;

import com.gustavosiqueira.payment.transaction.adapters.in.controller.dto.CreateTransactionRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    private UUID id;
    @Column(name = "from_wallet_id")
    private UUID fromWalletId;
    @Column(name = "to_wallet_id")
    private UUID toWalletId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    @Column(name = "created_at")
    private Instant createdAt;

    public static Transaction fromCreateTransactionRequest(CreateTransactionRequest createTransactionRequest) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .fromWalletId(createTransactionRequest.fromWalletId())
                .toWalletId(createTransactionRequest.toWalletId())
                .amount(createTransactionRequest.amount())
                .status(TransactionStatus.CREATED)
                .createdAt(Instant.now())
                .build();
    }
}
