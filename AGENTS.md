# Repository Guidelines

## Project Structure & Module Organization

InvoiceForge is a Java 17 multi-module Maven project. The root `pom.xml` defines shared dependency versions and includes these modules:

- `invoiceforge-core`: domain models, invoice numbering, money utilities, VAT, discount, and total calculations.
- `invoiceforge-pdf`: PDF generation using Apache PDFBox.
- `invoiceforge-export`: JSON and CSV exporters.
- `invoiceforge-cli`: command-line entry point.
- `invoiceforge-mcp`: lightweight HTTP tool server.
- `examples/`: sample invoice input such as `examples/invoice.json`.

Production code lives under each module's `src/main/java/ch/invoiceforge/...` tree. Tests live under matching `src/test/java` trees.

## Build, Test, and Development Commands

Run commands from the repository root unless noted otherwise.

- `mvn clean test`: compile all modules and run JUnit tests.
- `mvn clean package`: build all artifacts, including CLI and server jars.
- `mvn -pl invoiceforge-core test`: run tests for one module.
- `java -jar invoiceforge-cli/target/invoiceforge-cli-0.1.0-SNAPSHOT.jar total examples/invoice.json`: run the CLI against the sample invoice after packaging.
- `java -cp invoiceforge-mcp/target/invoiceforge-mcp-0.1.0-SNAPSHOT.jar ch.invoiceforge.mcp.InvoiceForgeMcpServer 8088`: start the MCP server after packaging.

## Coding Style & Naming Conventions

Use Java 17 features where they improve clarity. Keep packages under `ch.invoiceforge.<module>`. Use 4-space indentation and standard Java naming: `PascalCase` for classes, `camelCase` for methods and fields, and `UPPER_SNAKE_CASE` for constants. Prefer small classes that match the existing calculator, model, exporter, and generator organization.

No formatter or lint plugin is configured in Maven. Keep formatting consistent with existing source files and avoid unrelated style-only churn.

## Testing Guidelines

Tests use JUnit Jupiter via Maven Surefire. Place tests in the module they validate under `src/test/java`, mirroring the production package. Name test classes after the unit under test, for example `TotalCalculatorTest`. Add or update tests when changing calculations, export behavior, PDF decisions, CLI arguments, or MCP request handling.

## Commit & Pull Request Guidelines

This checkout does not include Git history, so no repository-specific commit convention can be inferred. Use short, imperative commit subjects such as `Add CSV export validation` or `Fix VAT rounding`.

Pull requests should include a concise summary, affected modules, test results such as `mvn clean test`, and any CLI/server examples used for manual verification. Include sample inputs or output notes for changes that affect PDFs, JSON, CSV, or HTTP responses.

## Security & Configuration Tips

Do not commit generated invoices, customer data, private keys, or build artifacts. Keep examples synthetic. Validate file paths and request inputs in CLI and MCP code before adding features that read or write user-provided locations.
