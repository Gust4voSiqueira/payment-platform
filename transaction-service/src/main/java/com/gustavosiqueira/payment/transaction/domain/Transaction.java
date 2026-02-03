package com.gustavosiqueira.payment.transaction.domain;

import com.gustavosiqueira.payment.transaction.adapters.in.controller.dto.CreateTransactionRequest;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    private UUID fromWalletId;
    private UUID toWalletId;
    private BigDecimal amount;
    private TransactionStatus status;
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
