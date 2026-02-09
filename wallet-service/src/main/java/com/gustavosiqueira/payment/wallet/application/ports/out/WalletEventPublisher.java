package com.gustavosiqueira.payment.wallet.application.ports.out;

import com.gustavosiqueira.payment.wallet.application.event.WalletBalanceReservedEvent;

public interface WalletEventPublisher {

    void sendWallet(WalletBalanceReservedEvent event, String eventType);
}
