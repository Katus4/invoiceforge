package ch.invoiceforge.core.number;

import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;

public class InvoiceNumberGenerator {
    private final String prefix;
    private final AtomicInteger sequence;

    public InvoiceNumberGenerator() {
        this("RE", 1);
    }

    public InvoiceNumberGenerator(String prefix, int start) {
        this.prefix = prefix;
        this.sequence = new AtomicInteger(start);
    }

    public String next() {
        return next(Year.now().getValue());
    }

    public String next(int year) {
        return "%s-%d-%04d".formatted(prefix, year, sequence.getAndIncrement());
    }
}
