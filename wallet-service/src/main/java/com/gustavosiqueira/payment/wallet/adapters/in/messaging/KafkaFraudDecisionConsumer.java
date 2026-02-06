package com.gustavosiqueira.payment.wallet.adapters.in.messaging;

import com.gustavosiqueira.payment.wallet.application.event.FraudDecisionEvent;
import com.gustavosiqueira.payment.wallet.application.ports.in.FraudDecisionConsumer;
import com.gustavosiqueira.payment.wallet.application.use_case.ProcessFraudDecisionUseCase;
import com.gustavosiqueira.payment.wallet.application.use_case.ReserveWalletBalanceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static com.gustavosiqueira.payment.wallet.domain.FraudDecision.APPROVED;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaFraudDecisionConsumer implements FraudDecisionConsumer {

    private final ProcessFraudDecisionUseCase processFraudDecisionUseCase;

    @Bean
    @Override
    public Consumer<FraudDecisionEvent> fraudDecision() {
        return event -> {
            if(event.decision().equals(APPROVED.name())) {
                log.info(
                        "[KafkaFraudDecisionConsumer.fraudDecision] Event FraudDecision received | transactionId={} | riskScore={} | decision={} | analysedAt={}",
                        event.transactionId(),
                        event.riskScore(),
                        event.decision(),
                        event.analysedAt()
                );

                try {
                    processFraudDecisionUseCase.execute(event);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
