package com.gustavosiqueira.payment.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Entity(name = "wallets")
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {

    @Id
    private UUID id;
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "available_balance")
    private BigDecimal availableBalance;
    @Column(name = "reserved_balance")
    private BigDecimal reservedBalance;

    public static Wallet from(UUID walletId, UUID userId, BigDecimal availableBalance, BigDecimal reservedBalance) {
        return Wallet.builder()
                .id(walletId)
                .userId(userId)
                .availableBalance(availableBalance)
                .reservedBalance(reservedBalance)
                .build();
    }
}
