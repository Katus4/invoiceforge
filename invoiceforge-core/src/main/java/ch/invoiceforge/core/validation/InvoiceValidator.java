package ch.invoiceforge.core.validation;

import ch.invoiceforge.core.model.Address;
import ch.invoiceforge.core.model.Company;
import ch.invoiceforge.core.model.Customer;
import ch.invoiceforge.core.model.Invoice;
import ch.invoiceforge.core.model.InvoiceItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class InvoiceValidator {
    private static final Pattern CURRENCY = Pattern.compile("[A-Z]{3}");
    private static final Pattern COUNTRY = Pattern.compile("[A-Z]{2}");
    private static final Pattern BASIC_IBAN = Pattern.compile("[A-Z]{2}[0-9A-Z]{13,32}");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private InvoiceValidator() {
    }

    public static List<ValidationIssue> validate(Invoice invoice) {
        List<ValidationIssue> issues = new ArrayList<>();
        if (invoice == null) {
            issues.add(new ValidationIssue("invoice", "must not be null"));
            return issues;
        }

        requirePresent(issues, "documentType", invoice.getDocumentType());
        requireText(issues, "invoiceNumber", invoice.getInvoiceNumber());
        requirePresent(issues, "invoiceDate", invoice.getInvoiceDate());
        requirePresent(issues, "dueDate", invoice.getDueDate());
        validateDueDate(issues, invoice.getInvoiceDate(), invoice.getDueDate());
        validateCurrency(issues, invoice.getCurrency());
        validateCompany(issues, invoice.getCompany());
        validateCustomer(issues, invoice.getCustomer());
        validateItems(issues, invoice.getItems());
        return List.copyOf(issues);
    }

    public static void requireValid(Invoice invoice) {
        List<ValidationIssue> issues = validate(invoice);
        if (!issues.isEmpty()) {
            throw new InvoiceValidationException(issues);
        }
    }

    private static void validateCompany(List<ValidationIssue> issues, Company company) {
        if (company == null) {
            issues.add(new ValidationIssue("company", "must not be null"));
            return;
        }
        requireText(issues, "company.name", company.getName());
        validateAddress(issues, "company.address", company.getAddress());
        if (hasText(company.getIban())) {
            String iban = company.getIban().replace(" ", "").toUpperCase();
            if (!BASIC_IBAN.matcher(iban).matches()) {
                issues.add(new ValidationIssue("company.iban", "must look like a valid IBAN"));
            }
        }
    }

    private static void validateCustomer(List<ValidationIssue> issues, Customer customer) {
        if (customer == null) {
            issues.add(new ValidationIssue("customer", "must not be null"));
            return;
        }
        requireText(issues, "customer.name", customer.getName());
        validateAddress(issues, "customer.address", customer.getAddress());
    }

    private static void validateAddress(List<ValidationIssue> issues, String path, Address address) {
        if (address == null) {
            issues.add(new ValidationIssue(path, "must not be null"));
            return;
        }
        requireText(issues, path + ".street", address.getStreet());
        requireText(issues, path + ".postalCode", address.getPostalCode());
        requireText(issues, path + ".city", address.getCity());
        requireText(issues, path + ".country", address.getCountry());
        if (hasText(address.getCountry()) && !COUNTRY.matcher(address.getCountry()).matches()) {
            issues.add(new ValidationIssue(path + ".country", "must be an ISO 3166-1 alpha-2 country code"));
        }
    }

    private static void validateItems(List<ValidationIssue> issues, List<InvoiceItem> items) {
        if (items == null || items.isEmpty()) {
            issues.add(new ValidationIssue("items", "must contain at least one item"));
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            InvoiceItem item = items.get(i);
            String path = "items[" + i + "]";
            if (item == null) {
                issues.add(new ValidationIssue(path, "must not be null"));
                continue;
            }
            requireText(issues, path + ".description", item.getDescription());
            requirePositive(issues, path + ".quantity", item.getQuantity());
            requireNonNegative(issues, path + ".unitPrice", item.getUnitPrice());
            validatePercent(issues, path + ".vatRate", item.getVatRate());
            validatePercent(issues, path + ".discountRate", item.getDiscountRate());
        }
    }

    private static void validateDueDate(List<ValidationIssue> issues, LocalDate invoiceDate, LocalDate dueDate) {
        if (invoiceDate != null && dueDate != null && dueDate.isBefore(invoiceDate)) {
            issues.add(new ValidationIssue("dueDate", "must not be before invoiceDate"));
        }
    }

    private static void validateCurrency(List<ValidationIssue> issues, String currency) {
        if (!hasText(currency)) {
            issues.add(new ValidationIssue("currency", "must not be blank"));
        } else if (!CURRENCY.matcher(currency).matches()) {
            issues.add(new ValidationIssue("currency", "must be an ISO 4217 currency code"));
        }
    }

    private static void requireText(List<ValidationIssue> issues, String path, String value) {
        if (!hasText(value)) {
            issues.add(new ValidationIssue(path, "must not be blank"));
        }
    }

    private static void requirePresent(List<ValidationIssue> issues, String path, Object value) {
        if (value == null) {
            issues.add(new ValidationIssue(path, "must not be null"));
        }
    }

    private static void requirePositive(List<ValidationIssue> issues, String path, BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            issues.add(new ValidationIssue(path, "must be greater than zero"));
        }
    }

    private static void requireNonNegative(List<ValidationIssue> issues, String path, BigDecimal value) {
        if (value == null || value.signum() < 0) {
            issues.add(new ValidationIssue(path, "must not be negative"));
        }
    }

    private static void validatePercent(List<ValidationIssue> issues, String path, BigDecimal value) {
        if (value == null || value.signum() < 0 || value.compareTo(ONE_HUNDRED) > 0) {
            issues.add(new ValidationIssue(path, "must be between 0 and 100"));
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
