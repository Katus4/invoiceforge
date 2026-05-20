package ch.invoiceforge.core.number;

import ch.invoiceforge.core.model.Invoice;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class InvoiceNumberGenerator {
    public static final String DEFAULT_PATTERN = "{prefix}-{year}-{seq}";

    private final String prefix;
    private final AtomicInteger sequence;
    private final String pattern;
    private final int sequenceWidth;

    public InvoiceNumberGenerator() {
        this("RE", 1, DEFAULT_PATTERN, 4);
    }

    public InvoiceNumberGenerator(String prefix, int start) {
        this(prefix, start, DEFAULT_PATTERN, 4);
    }

    public InvoiceNumberGenerator(String prefix, int start, String pattern) {
        this(prefix, start, pattern, 4);
    }

    public InvoiceNumberGenerator(String prefix, int start, String pattern, int sequenceWidth) {
        this.prefix = prefix;
        this.sequence = new AtomicInteger(start);
        this.pattern = pattern == null || pattern.isBlank() ? DEFAULT_PATTERN : pattern;
        this.sequenceWidth = Math.max(1, sequenceWidth);
    }

    public String next() {
        return next(LocalDate.now());
    }

    public String next(int year) {
        return next(LocalDate.of(year, 1, 1));
    }

    public String next(LocalDate date) {
        LocalDate effectiveDate = date == null ? LocalDate.now() : date;
        int value = sequence.getAndIncrement();
        String paddedSequence = "%0" + sequenceWidth + "d";
        return pattern
                .replace("{prefix}", prefix)
                .replace("{year}", String.valueOf(effectiveDate.getYear()))
                .replace("{shortYear}", "%02d".formatted(effectiveDate.getYear() % 100))
                .replace("{month}", "%02d".formatted(effectiveDate.getMonthValue()))
                .replace("{day}", "%02d".formatted(effectiveDate.getDayOfMonth()))
                .replace("{seq}", paddedSequence.formatted(value))
                .replace("{sequence}", String.valueOf(value));
    }

    public Invoice assignIfMissing(Invoice invoice) {
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber(next(invoice.getInvoiceDate()));
        }
        return invoice;
    }
}
