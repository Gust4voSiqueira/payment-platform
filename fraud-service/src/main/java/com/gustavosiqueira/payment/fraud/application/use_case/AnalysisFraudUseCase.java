package com.gustavosiqueira.payment.fraud.application.use_case;

import com.gustavosiqueira.payment.fraud.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.fraud.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.fraud.application.ports.out.FraudAnalysisRepository;
import com.gustavosiqueira.payment.fraud.application.ports.out.FraudDecisionEventPublisher;
import com.gustavosiqueira.payment.fraud.domain.FraudDecision;
import com.gustavosiqueira.payment.fraud.domain.FraudReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.gustavosiqueira.payment.fraud.domain.FraudAnalysis.fromWalletBalanceReservedEvent;

@Service
@RequiredArgsConstructor
public class AnalysisFraudUseCase implements UseCase<WalletBalanceReservedEvent> {

    private final FraudAnalysisRepository fraudAnalysisRepository;
    private final FraudDecisionEventPublisher fraudDecisionEventPublisher;

    public static final int SCORE_REVIEW = 40;
    public static final int SCORE_REJECTED = 70;
    public static final int SCORE_HIGH_VALUE = 40;
    public static final int SCORE_HIGH_REJECTED = 30;
    public static final int SCORE_HIGH_APPROVED = 20;
    private static final int MAX_TRANSACTIONS_FOR_DAY = 5;
    private static final BigDecimal AMOUNT_MAX_SUSPECT = new BigDecimal("10000");

    @Override
    public void execute(WalletBalanceReservedEvent input) {
        var score = 0;

        var highValue = isHighValue(input.reservedAmount());
        var highRejected = isHighFrequencyRejected(input.userFromId());
        var highApproved = isHighFrequencyApproved(input.userFromId());

        if (highValue) score += SCORE_HIGH_VALUE;
        if (highRejected) score += SCORE_HIGH_REJECTED;
        if (highApproved) score += SCORE_HIGH_APPROVED;

        var decision = decide(score);
        var reason = resolveReason(highValue, highRejected, highApproved);

        var analysis = fromWalletBalanceReservedEvent(input, decision, reason);
        fraudAnalysisRepository.save(analysis);
        var fraudDecisionEvent = new FraudDecisionEvent(
                analysis.getTransactionId(),
                UUID.randomUUID(),
                input.userFromId(),
                input.userToId(),
                input.reservedAmount(),
                score,
                decision.name(),
                analysis.getAnalyzedAt()
        );
        fraudDecisionEventPublisher.fraudDecision(fraudDecisionEvent);
    }

    private boolean isHighValue(BigDecimal reservedAmount) {
        return reservedAmount.compareTo(AMOUNT_MAX_SUSPECT) > 0;
    }

    private boolean isHighFrequencyApproved(UUID userId) {
        var last24Hours = Instant.now().minus(24, ChronoUnit.HOURS);
        return fraudAnalysisRepository
                .countByUserIdAndFraudDecisionAndAnalyzedAtAfter(
                        userId,
                        FraudDecision.APPROVED,
                        last24Hours
                ) > MAX_TRANSACTIONS_FOR_DAY;
    }

    private boolean isHighFrequencyRejected(UUID userId) {
        var last24Hours = Instant.now().minus(24, ChronoUnit.HOURS);
        return fraudAnalysisRepository
                .countByUserIdAndFraudDecisionAndAnalyzedAtAfter(
                        userId,
                        FraudDecision.REJECTED,
                        last24Hours
                ) > MAX_TRANSACTIONS_FOR_DAY;
    }

    private FraudReason resolveReason(
            boolean highValue,
            boolean highRejected,
            boolean highApproved
    ) {
        if (highValue && (highRejected || highApproved)) {
            return FraudReason.COMBINED_RISK_FACTORS;
        }

        if (highValue) {
            return FraudReason.HIGH_VALUE_TRANSACTION;
        }

        if (highRejected) {
            return FraudReason.HIGH_FREQUENCY_REJECTED;
        }

        if (highApproved) {
            return FraudReason.HIGH_FREQUENCY_APPROVED;
        }

        return FraudReason.NO_RISK_DETECTED;
    }

    private FraudDecision decide(int score) {
        if (score >= SCORE_REJECTED) {
            return FraudDecision.REJECTED;
        }

        if (score >= SCORE_REVIEW) {
            return FraudDecision.REVIEW;
        }

        return FraudDecision.APPROVED;
    }
}
