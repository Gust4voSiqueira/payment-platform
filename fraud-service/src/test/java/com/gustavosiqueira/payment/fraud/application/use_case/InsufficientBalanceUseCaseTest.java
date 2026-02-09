package com.gustavosiqueira.payment.fraud.application.use_case;

import com.gustavosiqueira.payment.fraud.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.fraud.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.fraud.application.ports.out.FraudDecisionEventPublisher;
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

import static com.gustavosiqueira.payment.fraud.domain.WalletEventType.INSUFFICIENT_BALANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InsufficientBalanceUseCaseTest {

    @InjectMocks
    private InsufficientBalanceUseCase useCase;

    @Mock
    private FraudDecisionEventPublisher fraudDecisionEventPublisher;

    @Test
    @DisplayName("Deve publicar evento de decisão de fraude por saldo insuficiente")
    void shouldPublishFraudDecisionEventWhenInsufficientBalance() {
        var event = buildEvent();

        useCase.execute(event);

        var fraudDecisionEvent = captureFraudDecisionEvent();

        assertThat(fraudDecisionEvent.transactionId()).isEqualTo(event.transactionId());
        assertThat(fraudDecisionEvent.userFromId()).isEqualTo(event.userFromId());
        assertThat(fraudDecisionEvent.userToId()).isEqualTo(event.userToId());
        assertThat(fraudDecisionEvent.amount()).isEqualTo(event.reservedAmount());
        assertThat(fraudDecisionEvent.riskScore()).isZero();
        assertThat(fraudDecisionEvent.decision()).isEqualTo(INSUFFICIENT_BALANCE.name());
        assertThat(fraudDecisionEvent.analysedAt()).isNotNull();
    }

    private FraudDecisionEvent captureFraudDecisionEvent() {
        var captor = ArgumentCaptor.forClass(FraudDecisionEvent.class);
        verify(fraudDecisionEventPublisher).fraudDecision(captor.capture());
        return captor.getValue();
    }

    private WalletBalanceReservedEvent buildEvent() {
        return new WalletBalanceReservedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100"),
                "BRL",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Instant.now()
        );
    }
}