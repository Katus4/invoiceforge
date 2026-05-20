package ch.invoiceforge.export;

import ch.invoiceforge.core.calc.InvoiceItemTotals;
import ch.invoiceforge.core.calc.InvoiceTotals;
import ch.invoiceforge.core.calc.TotalCalculator;
import ch.invoiceforge.core.model.Invoice;
import ch.invoiceforge.core.model.InvoiceItem;
import ch.invoiceforge.core.validation.InvoiceValidator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class InvoiceCsvExporter {
    private InvoiceCsvExporter() {
    }

    public static void export(Invoice invoice, String outputPath) throws IOException {
        export(invoice, Path.of(outputPath));
    }

    public static void export(Invoice invoice, Path outputPath) throws IOException {
        InvoiceValidator.requireValid(invoice);
        try (Writer writer = Files.newBufferedWriter(outputPath);
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("type", "invoiceNumber", "date", "dueDate", "description", "quantity",
                             "unitPrice", "discountRate", "vatRate", "netAmount", "vatAmount", "grossAmount")
                     .build())) {
            for (InvoiceItem item : invoice.getItems()) {
                InvoiceItemTotals totals = TotalCalculator.calculateItem(item);
                csv.printRecord(
                        invoice.getDocumentType(),
                        invoice.getInvoiceNumber(),
                        invoice.getInvoiceDate(),
                        invoice.getDueDate(),
                        item.getDescription(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getDiscountRate(),
                        item.getVatRate(),
                        totals.netAmount(),
                        totals.vatAmount(),
                        totals.grossAmount()
                );
            }
            InvoiceTotals totals = TotalCalculator.calculate(invoice);
            csv.printRecord("TOTAL", invoice.getInvoiceNumber(), "", "", "", "", "", "", "",
                    totals.netTotal(), totals.vatTotal(), totals.grossTotal());
        }
    }
}
