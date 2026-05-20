# InvoiceForge for Java

[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/)
[![Build](https://img.shields.io/badge/build-Maven-blue)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/license-PolyForm%20Noncommercial-orange)](LICENSE)

Source-available Java library and command-line toolkit for creating invoices, quotes, receipts, PDF documents, and invoice exports.

InvoiceForge is designed as a small modular foundation for business document generation. It includes a reusable domain model, calculation utilities, PDF rendering, JSON/CSV export, a CLI, and a lightweight HTTP tool server for automation.

## Features

InvoiceForge currently supports:

- generating invoice, quote, and receipt PDFs
- calculating net, VAT, and gross totals
- item-level VAT and discount handling
- exporting invoices as JSON and CSV
- loading invoice data from JSON
- using a command-line interface for local automation
- exposing document tools through a simple MCP-style HTTP server
- running as a Java 17 Maven multi-module project

## Modules

- `invoiceforge-core` - domain model, totals, VAT, discounts, and invoice numbers
- `invoiceforge-pdf` - PDF generation with Apache PDFBox
- `invoiceforge-export` - JSON and CSV import/export
- `invoiceforge-cli` - runnable command-line application
- `invoiceforge-mcp` - HTTP tool server for automation
- `examples` - sample invoice input files

## Getting Started

Build the project with Maven:

```bash
mvn clean package
```

Run the test suite:

```bash
mvn clean test
```

The CLI jar is created at:

```text
invoiceforge-cli/target/invoiceforge-cli-0.1.0-SNAPSHOT.jar
```

## CLI Usage

Generate an invoice PDF:

```bash
java -jar invoiceforge-cli/target/invoiceforge-cli-0.1.0-SNAPSHOT.jar create-invoice examples/invoice.json invoice.pdf
```

Generate a quote or receipt:

```bash
java -jar invoiceforge-cli/target/invoiceforge-cli-0.1.0-SNAPSHOT.jar create-quote examples/invoice.json quote.pdf
java -jar invoiceforge-cli/target/invoiceforge-cli-0.1.0-SNAPSHOT.jar create-receipt examples/invoice.json receipt.pdf
```

Calculate totals and export data:

```bash
java -jar invoiceforge-cli/target/invoiceforge-cli-0.1.0-SNAPSHOT.jar total examples/invoice.json
java -jar invoiceforge-cli/target/invoiceforge-cli-0.1.0-SNAPSHOT.jar export-csv examples/invoice.json invoice.csv
java -jar invoiceforge-cli/target/invoiceforge-cli-0.1.0-SNAPSHOT.jar export-json examples/invoice.json invoice-out.json
```

## Java Example

```java
import ch.invoiceforge.core.model.Company;
import ch.invoiceforge.core.model.Customer;
import ch.invoiceforge.core.model.Invoice;
import ch.invoiceforge.core.model.InvoiceItem;
import ch.invoiceforge.pdf.InvoicePdfGenerator;

import java.time.LocalDate;

Invoice invoice = new Invoice()
        .setInvoiceNumber("RE-2026-0001")
        .setInvoiceDate(LocalDate.now())
        .setDueDate(LocalDate.now().plusDays(30))
        .setCurrency("CHF")
        .setCompany(new Company("Demo GmbH", "Industriestrasse 5", "3000 Bern")
                .setEmail("info@example.com")
                .setIban("CH93 0076 2011 6238 5295 7"))
        .setCustomer(new Customer("Max Mueller", "Bahnhofstrasse 1", "8001 Zuerich"))
        .addItem(new InvoiceItem("Webdesign", 10, 85.00, 8.1))
        .addItem(new InvoiceItem("Hosting", 1, 120.00, 8.1));

InvoicePdfGenerator.generate(invoice, "invoice.pdf");
```

## HTTP Tool Server

Start the server:

```bash
java -cp invoiceforge-mcp/target/invoiceforge-mcp-0.1.0-SNAPSHOT.jar ch.invoiceforge.mcp.InvoiceForgeMcpServer 8088
```

Available endpoints:

- `GET /tools`
- `POST /calculate_invoice_total`
- `POST /export_invoice_json`
- `POST /create_invoice_pdf`
- `POST /create_quote_pdf`
- `POST /create_receipt_pdf`

## Example Input

See [`examples/invoice.json`](examples/invoice.json) for a complete invoice payload with company, customer, items, VAT rates, and payment information.

## Requirements

- Java 17 or newer
- Maven 3.9 or newer

## Project Status

InvoiceForge is an early-stage library. The public API may still change before a stable release. Maven Central publishing is not configured yet; build from source for now.

## License

InvoiceForge is released under the [PolyForm Noncommercial License 1.0.0](LICENSE).
Commercial use, including selling the library or products/services based on it, requires a separate license from the copyright holder.
