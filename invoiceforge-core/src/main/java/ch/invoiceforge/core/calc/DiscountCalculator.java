package ch.invoiceforge.core.calc;

import ch.invoiceforge.core.util.Money;

import java.math.BigDecimal;

public final class DiscountCalculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private DiscountCalculator() {
    }

    public static BigDecimal calculate(BigDecimal amount, BigDecimal discountRate) {
        Money.requireNonNegative(amount, "amount");
        Money.requireNonNegative(discountRate, "discountRate");
        return Money.round(amount.multiply(discountRate).divide(ONE_HUNDRED, Money.CALCULATION_CONTEXT));
    }
}
