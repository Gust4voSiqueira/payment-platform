package com.gustavosiqueira.payment.wallet.domain;

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
@Entity(name = "wallet_reservations")
@AllArgsConstructor
@NoArgsConstructor
public class WalletReservation {

    @Id
    private UUID id;
    @Column(name = "transaction_id")
    private UUID transactionId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private WalletReservationStatus status;
    @Column(name = "created_at")
    private Instant createdAt;

    public static WalletReservation from(UUID id, UUID transactionId, BigDecimal amount, WalletReservationStatus status, Instant createdAt) {
        return WalletReservation.builder()
                .id(id)
                .transactionId(transactionId)
                .amount(amount)
                .status(status)
                .createdAt(createdAt)
                .build();
    }
}
