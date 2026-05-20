package ch.invoiceforge.core.calc;

import java.math.BigDecimal;

public record InvoiceItemTotals(
        BigDecimal lineSubtotal,
        BigDecimal discountAmount,
        BigDecimal netAmount,
        BigDecimal vatAmount,
        BigDecimal grossAmount
) {
}
