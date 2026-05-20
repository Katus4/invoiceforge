package ch.invoiceforge.core.validation;

import java.util.List;
import java.util.stream.Collectors;

public class InvoiceValidationException extends IllegalArgumentException {
    private final List<ValidationIssue> issues;

    public InvoiceValidationException(List<ValidationIssue> issues) {
        super(issues.stream()
                .map(ValidationIssue::toString)
                .collect(Collectors.joining(System.lineSeparator(), "Invoice validation failed:" + System.lineSeparator(), "")));
        this.issues = List.copyOf(issues);
    }

    public List<ValidationIssue> getIssues() {
        return issues;
    }
}
