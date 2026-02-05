package com.gustavosiqueira.payment.fraud.application.use_case;

import com.gustavosiqueira.payment.fraud.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.fraud.application.ports.out.FraudAnalysisRepository;
import com.gustavosiqueira.payment.fraud.domain.FraudAnalysis;
import com.gustavosiqueira.payment.fraud.domain.FraudDecision;
import com.gustavosiqueira.payment.fraud.domain.FraudReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalysisFraudUseCaseTest {

    @InjectMocks
    private AnalysisFraudUseCase useCase;

    @Mock
    private FraudAnalysisRepository fraudAnalysisRepository;

    @Test
    @DisplayName("Deve aprovar transação sem risco")
    void shouldApproveTransactionWhenNoRiskDetected() {
        var event = buildEvent(new BigDecimal("100"));

        when(fraudAnalysisRepository.countByUserIdAndFraudDecisionAndAnalyzedAtAfter(
                eq(event.userId()),
                eq(FraudDecision.APPROVED),
                any(Instant.class)
        )).thenReturn(0L);

        when(fraudAnalysisRepository.countByUserIdAndFraudDecisionAndAnalyzedAtAfter(
                eq(event.userId()),
                eq(FraudDecision.REJECTED),
                any(Instant.class)
        )).thenReturn(0L);

        useCase.execute(event);

        var captor = ArgumentCaptor.forClass(FraudAnalysis.class);
        verify(fraudAnalysisRepository).save(captor.capture());

        var analysis = captor.getValue();

        assertThat(analysis.getFraudDecision()).isEqualTo(FraudDecision.APPROVED);
        assertThat(analysis.getReason()).isEqualTo(FraudReason.NO_RISK_DETECTED);
        assertThat(analysis.getTransactionId()).isEqualTo(event.transactionId());
    }

    @Test
    @DisplayName("Deve enviar para review quando valor alto")
    void shouldSendToReviewWhenHighValueTransaction() {
        var event = buildEvent(new BigDecimal("15000"));

        when(fraudAnalysisRepository.countByUserIdAndFraudDecisionAndAnalyzedAtAfter(
                eq(event.userId()),
                eq(FraudDecision.APPROVED),
                any(Instant.class)
        )).thenReturn(0L);

        when(fraudAnalysisRepository.countByUserIdAndFraudDecisionAndAnalyzedAtAfter(
                eq(event.userId()),
                eq(FraudDecision.REJECTED),
                any(Instant.class)
        )).thenReturn(0L);

        useCase.execute(event);

        var captor = ArgumentCaptor.forClass(FraudAnalysis.class);
        verify(fraudAnalysisRepository).save(captor.capture());

        var analysis = captor.getValue();

        assertThat(analysis.getFraudDecision()).isEqualTo(FraudDecision.REVIEW);
        assertThat(analysis.getReason()).isEqualTo(FraudReason.HIGH_VALUE_TRANSACTION);
    }

    @Test
    @DisplayName("Deve rejeitar transação com múltiplos fatores de risco")
    void shouldRejectWhenCombinedRiskFactors() {
        var event = buildEvent(new BigDecimal("15000"));

        when(fraudAnalysisRepository.countByUserIdAndFraudDecisionAndAnalyzedAtAfter(
                eq(event.userId()),
                eq(FraudDecision.APPROVED),
                any(Instant.class)
        )).thenReturn(0L);

        when(fraudAnalysisRepository.countByUserIdAndFraudDecisionAndAnalyzedAtAfter(
                eq(event.userId()),
                eq(FraudDecision.REJECTED),
                any(Instant.class)
        )).thenReturn(10L);

        useCase.execute(event);

        var captor = ArgumentCaptor.forClass(FraudAnalysis.class);
        verify(fraudAnalysisRepository).save(captor.capture());

        var analysis = captor.getValue();

        assertThat(analysis.getFraudDecision()).isEqualTo(FraudDecision.REJECTED);
        assertThat(analysis.getReason()).isEqualTo(FraudReason.COMBINED_RISK_FACTORS);
    }

    @Test
    @DisplayName("Deve enviar para review quando alta frequência de aprovadas")
    void shouldSendToReviewWhenHighApprovedFrequency() {
        var event = buildEvent(new BigDecimal("500"));

        when(fraudAnalysisRepository.countByUserIdAndFraudDecisionAndAnalyzedAtAfter(
                eq(event.userId()),
                eq(FraudDecision.APPROVED),
                any(Instant.class)
        )).thenReturn(10L);

        when(fraudAnalysisRepository.countByUserIdAndFraudDecisionAndAnalyzedAtAfter(
                eq(event.userId()),
                eq(FraudDecision.REJECTED),
                any(Instant.class)
        )).thenReturn(0L);

        useCase.execute(event);

        var captor = ArgumentCaptor.forClass(FraudAnalysis.class);
        verify(fraudAnalysisRepository).save(captor.capture());

        var analysis = captor.getValue();

        assertThat(analysis.getFraudDecision()).isEqualTo(FraudDecision.APPROVED);
        assertThat(analysis.getReason()).isEqualTo(FraudReason.HIGH_FREQUENCY_APPROVED);
    }

    private WalletBalanceReservedEvent buildEvent(BigDecimal amount) {
        return new WalletBalanceReservedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                amount,
                "BRL",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Instant.now()
        );
    }
}