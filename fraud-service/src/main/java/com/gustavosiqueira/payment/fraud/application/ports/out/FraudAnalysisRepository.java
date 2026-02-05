package com.gustavosiqueira.payment.fraud.application.ports.out;

import com.gustavosiqueira.payment.fraud.domain.FraudAnalysis;
import com.gustavosiqueira.payment.fraud.domain.FraudDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface FraudAnalysisRepository extends JpaRepository<FraudAnalysis, UUID> {

    long countByUserIdAndFraudDecisionAndAnalyzedAtAfter(UUID userId, FraudDecision decision, Instant analyzedAt);
}
