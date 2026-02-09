package com.gustavosiqueira.payment.fraud.adapters.in.messaging;

import com.gustavosiqueira.payment.fraud.application.event.WalletBalanceReservedEvent;
import com.gustavosiqueira.payment.fraud.application.ports.in.WalletReservedEventConsumer;
import com.gustavosiqueira.payment.fraud.application.use_case.AnalysisFraudUseCase;
import com.gustavosiqueira.payment.fraud.application.use_case.InsufficientBalanceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static com.gustavosiqueira.payment.fraud.domain.WalletEventType.BALANCE_RESERVED;
import static com.gustavosiqueira.payment.fraud.domain.WalletEventType.INSUFFICIENT_BALANCE;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaWalletReservedEventConsumer implements WalletReservedEventConsumer {

    private final AnalysisFraudUseCase analysisFraudUseCase;
    private final InsufficientBalanceUseCase insufficientBalanceUseCase;

    @Bean
    @Override
    public Consumer<Message<WalletBalanceReservedEvent>> walletReserved() {
        return message -> {
            var event = message.getPayload();
            var eventType = message.getHeaders().get("event_type", String.class);

            if (INSUFFICIENT_BALANCE.name().equals(eventType)) {
                log.info(
                        "[KafkaWalletReservedEventConsumer.walletReserved] Ignoring event due to insufficient balance | transactionId={}",
                        event.transactionId()
                );
                insufficientBalanceUseCase.execute(event);
            }

            if(BALANCE_RESERVED.name().equals(eventType)) {
                log.info(
                        "[KafkaWalletReservedEventConsumer.walletReserved] Event received | transactionId={} | reservedAmount={} | availableBalanceAfter={} | reservedBalanceAfter={}",
                        event.transactionId(),
                        event.reservedAmount(),
                        event.availableBalanceAfter(),
                        event.reservedBalanceAfter()
                );

                analysisFraudUseCase.execute(event);
            }
        };
    }
}
