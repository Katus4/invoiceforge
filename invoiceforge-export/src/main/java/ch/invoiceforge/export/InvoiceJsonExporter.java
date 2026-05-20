package ch.invoiceforge.export;

import ch.invoiceforge.core.model.Invoice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Path;

public final class InvoiceJsonExporter {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private InvoiceJsonExporter() {
    }

    public static void export(Invoice invoice, String outputPath) throws IOException {
        export(invoice, Path.of(outputPath));
    }

    public static void export(Invoice invoice, Path outputPath) throws IOException {
        MAPPER.writeValue(outputPath.toFile(), invoice);
    }

    public static String toJson(Invoice invoice) throws IOException {
        return MAPPER.writeValueAsString(invoice);
    }

    public static Invoice read(Path inputPath) throws IOException {
        return MAPPER.readValue(inputPath.toFile(), Invoice.class);
    }
}
