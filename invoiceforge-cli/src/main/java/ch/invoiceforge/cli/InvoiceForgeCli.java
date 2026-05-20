package ch.invoiceforge.cli;

import ch.invoiceforge.core.calc.InvoiceTotals;
import ch.invoiceforge.core.calc.TotalCalculator;
import ch.invoiceforge.core.model.DocumentType;
import ch.invoiceforge.core.model.Invoice;
import ch.invoiceforge.core.validation.InvoiceValidationException;
import ch.invoiceforge.core.validation.InvoiceValidator;
import ch.invoiceforge.core.validation.ValidationIssue;
import ch.invoiceforge.export.InvoiceCsvExporter;
import ch.invoiceforge.export.InvoiceJsonExporter;
import ch.invoiceforge.pdf.InvoicePdfGenerator;

import java.nio.file.Path;
import java.util.List;

public final class InvoiceForgeCli {
    private InvoiceForgeCli() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "help".equals(args[0]) || "--help".equals(args[0])) {
            printUsage();
            return;
        }

        try {
            switch (args[0]) {
                case "create-invoice" -> {
                    requireArgs(args, 3);
                    createPdf(args[1], args[2], DocumentType.INVOICE);
                }
                case "create-quote" -> {
                    requireArgs(args, 3);
                    createPdf(args[1], args[2], DocumentType.QUOTE);
                }
                case "create-receipt" -> {
                    requireArgs(args, 3);
                    createPdf(args[1], args[2], DocumentType.RECEIPT);
                }
                case "export-json" -> {
                    requireArgs(args, 3);
                    Invoice invoice = readValidInvoice(args[1]);
                    InvoiceJsonExporter.export(invoice, args[2]);
                    System.out.println("JSON written: " + args[2]);
                }
                case "export-csv" -> {
                    requireArgs(args, 3);
                    Invoice invoice = readValidInvoice(args[1]);
                    InvoiceCsvExporter.export(invoice, args[2]);
                    System.out.println("CSV written: " + args[2]);
                }
                case "total" -> {
                    requireArgs(args, 2);
                    Invoice invoice = readValidInvoice(args[1]);
                    InvoiceTotals totals = TotalCalculator.calculate(invoice);
                    System.out.printf("net=%s vat=%s gross=%s%n",
                            totals.netTotal(), totals.vatTotal(), totals.grossTotal());
                }
                case "validate" -> {
                    requireArgs(args, 2);
                    validate(args[1]);
                }
                default -> {
                    System.err.println("Unknown command: " + args[0]);
                    printUsage();
                    System.exit(2);
                }
            }
        } catch (InvoiceValidationException ex) {
            System.err.println(ex.getMessage());
            System.exit(3);
        }
    }

    private static void createPdf(String inputPath, String outputPath, DocumentType documentType) throws Exception {
        Invoice invoice = InvoiceJsonExporter.read(Path.of(inputPath));
        invoice.setDocumentType(documentType);
        InvoiceValidator.requireValid(invoice);
        InvoicePdfGenerator.generate(invoice, outputPath);
        System.out.println("PDF written: " + outputPath);
    }

    private static Invoice readValidInvoice(String inputPath) throws Exception {
        Invoice invoice = InvoiceJsonExporter.read(Path.of(inputPath));
        InvoiceValidator.requireValid(invoice);
        return invoice;
    }

    private static void validate(String inputPath) throws Exception {
        Invoice invoice = InvoiceJsonExporter.read(Path.of(inputPath));
        List<ValidationIssue> issues = InvoiceValidator.validate(invoice);
        if (issues.isEmpty()) {
            System.out.println("Invoice is valid.");
            return;
        }
        for (ValidationIssue issue : issues) {
            System.err.println(issue);
        }
        System.exit(3);
    }

    private static void requireArgs(String[] args, int expected) {
        if (args.length != expected) {
            printUsage();
            throw new IllegalArgumentException("Expected " + (expected - 1) + " arguments for " + args[0]);
        }
    }

    private static void printUsage() {
        System.out.println("""
                InvoiceForge CLI
                Usage:
                  java -jar invoiceforge-cli.jar create-invoice invoice.json rechnung.pdf
                  java -jar invoiceforge-cli.jar create-quote invoice.json offerte.pdf
                  java -jar invoiceforge-cli.jar create-receipt invoice.json quittung.pdf
                  java -jar invoiceforge-cli.jar export-json invoice.json out.json
                  java -jar invoiceforge-cli.jar export-csv invoice.json out.csv
                  java -jar invoiceforge-cli.jar total invoice.json
                  java -jar invoiceforge-cli.jar validate invoice.json
                """);
    }
}
