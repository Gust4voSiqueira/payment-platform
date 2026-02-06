package com.gustavosiqueira.payment.wallet.application.ports.out;

import com.gustavosiqueira.payment.wallet.domain.WalletReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletReservationsRepository extends JpaRepository<WalletReservation, UUID> {

    Optional<WalletReservation> findAllByTransactionId(UUID transactionId);
}
