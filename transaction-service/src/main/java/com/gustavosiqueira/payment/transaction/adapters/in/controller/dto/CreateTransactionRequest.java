package com.gustavosiqueira.payment.transaction.adapters.in.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(
        UUID fromWalletId,
        UUID toWalletId,
        BigDecimal amount
) {}
