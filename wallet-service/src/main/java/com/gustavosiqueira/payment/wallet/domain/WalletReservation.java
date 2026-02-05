package com.gustavosiqueira.payment.wallet.domain;

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
@Entity(name = "wallet_reservations")
@AllArgsConstructor
@NoArgsConstructor
public class WalletReservation {

    @Id
    private UUID id;
    private UUID transaction_id;
    private BigDecimal amount;
    private WalletReservationStatus status;
    private Instant created_at;

    public static WalletReservation from(UUID transactionId, BigDecimal amount, WalletReservationStatus status, Instant createdAt) {
        return WalletReservation.builder()
                .id(UUID.randomUUID())
                .transaction_id(transactionId)
                .amount(amount)
                .status(status)
                .created_at(createdAt)
                .build();
    }
}
