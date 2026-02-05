package com.gustavosiqueira.payment.wallet.application.ports.out;

import com.gustavosiqueira.payment.wallet.application.event.WalletBalanceReservedEvent;

public interface WalletEventPublisher {

    void walletCreated(WalletBalanceReservedEvent event, String eventType);
}
