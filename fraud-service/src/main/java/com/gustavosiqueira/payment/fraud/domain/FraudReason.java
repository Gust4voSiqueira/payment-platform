package com.gustavosiqueira.payment.fraud.domain;

public enum FraudReason {
    HIGH_VALUE_TRANSACTION,
    HIGH_FREQUENCY_APPROVED,
    HIGH_FREQUENCY_REJECTED,
    COMBINED_RISK_FACTORS,
    NO_RISK_DETECTED
}
