package com.gustavosiqueira.payment.fraud.application.use_case;

import com.gustavosiqueira.payment.fraud.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.fraud.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.fraud.application.ports.out.FraudDecisionEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static com.gustavosiqueira.payment.fraud.domain.WalletEventType.INSUFFICIENT_BALANCE;

@Service
@RequiredArgsConstructor
public class InsufficientBalanceUseCase implements UseCase<WalletBalanceReservedEvent> {

    private final FraudDecisionEventPublisher fraudDecisionEventPublisher;

    private static final Integer DEFAULT_SCORE = 0;

    @Override
    public void execute(WalletBalanceReservedEvent input) {
        var fraudDecisionEvent = new FraudDecisionEvent(
                input.transactionId(),
                UUID.randomUUID(),
                input.userFromId(),
                input.userToId(),
                input.reservedAmount(),
                DEFAULT_SCORE,
                INSUFFICIENT_BALANCE.name(),
                Instant.now()
        );
        fraudDecisionEventPublisher.fraudDecision(fraudDecisionEvent);
    }
}
