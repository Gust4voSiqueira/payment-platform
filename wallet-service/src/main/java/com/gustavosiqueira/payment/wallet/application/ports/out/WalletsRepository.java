package com.gustavosiqueira.payment.wallet.application.ports.out;

import com.gustavosiqueira.payment.wallet.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletsRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findWalletsByUserId(UUID userId);
}
