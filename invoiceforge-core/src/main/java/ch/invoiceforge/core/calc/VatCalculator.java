package ch.invoiceforge.core.calc;

import ch.invoiceforge.core.util.Money;

import java.math.BigDecimal;

public final class VatCalculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private VatCalculator() {
    }

    public static BigDecimal calculate(BigDecimal netAmount, BigDecimal vatRate) {
        Money.requireNonNegative(netAmount, "netAmount");
        Money.requireNonNegative(vatRate, "vatRate");
        return Money.round(netAmount.multiply(vatRate).divide(ONE_HUNDRED, Money.CALCULATION_CONTEXT));
    }
}
