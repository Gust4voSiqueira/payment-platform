package com.gustavosiqueira.payment.wallet.application.exceptions;

public class WalletReservationNotFoundException extends Exception {

    public WalletReservationNotFoundException() {
        super("Wallet Reservation não encontrada");
    }
}