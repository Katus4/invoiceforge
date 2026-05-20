package ch.invoiceforge.core.calc;

import java.math.BigDecimal;
import java.util.Map;

public record InvoiceTotals(
        BigDecimal netTotal,
        BigDecimal discountTotal,
        BigDecimal vatTotal,
        BigDecimal grossTotal,
        Map<BigDecimal, BigDecimal> vatByRate
) {
}
