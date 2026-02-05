package com.gustavosiqueira.payment.fraud.adapters.in.messaging;

import com.gustavosiqueira.payment.fraud.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.fraud.application.ports.in.WalletReservedEventConsumer;
import com.gustavosiqueira.payment.fraud.application.use_case.AnalysisFraudUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaWalletReservedEventConsumer implements WalletReservedEventConsumer {

    private final AnalysisFraudUseCase analysisFraudUseCase;

    @Bean
    @Override
    public Consumer<WalletBalanceReservedEvent> walletReserved() {
        return event -> {
            log.info(
                    "[KafkaWalletReservedEventConsumer.walletReserved] Event TransactionCreated received | transactionId={} | reservedAmount={} | availableBalanceAfter={} | reservedBalanceAfter={}",
                    event.transactionId(),
                    event.reservedAmount(),
                    event.availableBalanceAfter(),
                    event.reservedBalanceAfter()
            );

            analysisFraudUseCase.execute(event);
        };
    }
}
