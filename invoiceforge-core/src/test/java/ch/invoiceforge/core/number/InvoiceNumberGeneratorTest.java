package ch.invoiceforge.core.number;

import ch.invoiceforge.core.model.Invoice;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceNumberGeneratorTest {
    @Test
    void generatesDefaultInvoiceNumbers() {
        InvoiceNumberGenerator generator = new InvoiceNumberGenerator("RE", 7);

        assertEquals("RE-2026-0007", generator.next(2026));
        assertEquals("RE-2026-0008", generator.next(2026));
    }

    @Test
    void supportsCustomPatternsAndSequenceWidth() {
        InvoiceNumberGenerator generator = new InvoiceNumberGenerator("INV", 42, "{prefix}/{shortYear}/{month}/{seq}", 3);

        assertEquals("INV/26/05/042", generator.next(LocalDate.of(2026, 5, 20)));
    }

    @Test
    void assignsOnlyMissingInvoiceNumber() {
        Invoice invoice = new Invoice().setInvoiceDate(LocalDate.of(2026, 5, 20));
        InvoiceNumberGenerator generator = new InvoiceNumberGenerator("RE", 1);

        generator.assignIfMissing(invoice);
        generator.assignIfMissing(invoice);

        assertEquals("RE-2026-0001", invoice.getInvoiceNumber());
    }
}
