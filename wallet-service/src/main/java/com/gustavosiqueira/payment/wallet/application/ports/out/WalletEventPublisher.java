package com.gustavosiqueira.payment.wallet.application.ports.out;

import com.gustavosiqueira.payment.wallet.application.event.WalletCreatedEvent;

public interface WalletEventPublisher {

    void walletCreated(WalletCreatedEvent event, String eventType);
}
