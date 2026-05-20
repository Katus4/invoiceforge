package ch.invoiceforge.core.validation;

public record ValidationIssue(String path, String message) {
    @Override
    public String toString() {
        return path + ": " + message;
    }
}
