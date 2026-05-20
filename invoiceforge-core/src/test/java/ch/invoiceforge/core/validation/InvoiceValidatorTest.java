package ch.invoiceforge.core.validation;

import ch.invoiceforge.core.model.Address;
import ch.invoiceforge.core.model.Company;
import ch.invoiceforge.core.model.Customer;
import ch.invoiceforge.core.model.Invoice;
import ch.invoiceforge.core.model.InvoiceItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceValidatorTest {
    @Test
    void acceptsCompleteInvoice() {
        Invoice invoice = validInvoice();

        assertDoesNotThrow(() -> InvoiceValidator.requireValid(invoice));
    }

    @Test
    void reportsMissingPartiesDatesAndInvalidItemValues() {
        Invoice invoice = new Invoice()
                .setInvoiceNumber(" ")
                .setInvoiceDate(LocalDate.of(2026, 5, 20))
                .setDueDate(LocalDate.of(2026, 5, 19))
                .setCurrency("CHF")
                .addItem(new InvoiceItem().setDescription("").setQuantity(0).setVatRate(101));

        List<ValidationIssue> issues = InvoiceValidator.validate(invoice);

        assertTrue(issues.stream().anyMatch(issue -> issue.path().equals("invoiceNumber")));
        assertTrue(issues.stream().anyMatch(issue -> issue.path().equals("dueDate")));
        assertTrue(issues.stream().anyMatch(issue -> issue.path().equals("company")));
        assertTrue(issues.stream().anyMatch(issue -> issue.path().equals("customer")));
        assertTrue(issues.stream().anyMatch(issue -> issue.path().equals("items[0].quantity")));
        assertTrue(issues.stream().anyMatch(issue -> issue.path().equals("items[0].vatRate")));
    }

    private static Invoice validInvoice() {
        return new Invoice()
                .setInvoiceNumber("RE-2026-0001")
                .setInvoiceDate(LocalDate.of(2026, 5, 20))
                .setDueDate(LocalDate.of(2026, 6, 19))
                .setCurrency("CHF")
                .setCompany(new Company("Demo GmbH", new Address("Industriestrasse 5", "3000", "Bern", "CH"))
                        .setIban("CH9300762011623852957"))
                .setCustomer(new Customer("Max Mueller", new Address("Bahnhofstrasse 1", "8001", "Zuerich", "CH")))
                .addItem(new InvoiceItem("Webdesign", 10, 85.00, 8.1));
    }
}
