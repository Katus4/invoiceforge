package ch.invoiceforge.core.calc;

import ch.invoiceforge.core.model.Invoice;
import ch.invoiceforge.core.model.InvoiceItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TotalCalculatorTest {
    @Test
    void calculatesQuantityDiscountVatAndGrossTotal() {
        Invoice invoice = new Invoice()
                .addItem(new InvoiceItem("Webdesign", 10, 85.00, 8.1).setDiscountRate(10))
                .addItem(new InvoiceItem("Hosting", 1, 120.00, 8.1));

        InvoiceTotals totals = TotalCalculator.calculate(invoice);

        assertEquals(new BigDecimal("885.00"), totals.netTotal());
        assertEquals(new BigDecimal("85.00"), totals.discountTotal());
        assertEquals(new BigDecimal("71.69"), totals.vatTotal());
        assertEquals(new BigDecimal("956.69"), totals.grossTotal());
    }
}
