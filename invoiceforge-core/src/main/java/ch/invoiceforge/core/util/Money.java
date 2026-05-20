package ch.invoiceforge.core.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class Money {
    public static final MathContext CALCULATION_CONTEXT = MathContext.DECIMAL64;

    private Money() {
    }

    public static BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value);
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }
}
