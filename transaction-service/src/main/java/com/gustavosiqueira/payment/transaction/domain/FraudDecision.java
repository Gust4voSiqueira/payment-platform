package com.gustavosiqueira.payment.transaction.domain;

public enum FraudDecision {
    APPROVED,
    REJECTED,
    REVIEW,
    INSUFFICIENT_BALANCE
}
