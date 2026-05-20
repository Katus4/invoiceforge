package ch.invoiceforge.mcp;

import ch.invoiceforge.core.calc.TotalCalculator;
import ch.invoiceforge.core.model.DocumentType;
import ch.invoiceforge.core.model.Invoice;
import ch.invoiceforge.core.validation.InvoiceValidationException;
import ch.invoiceforge.core.validation.InvoiceValidator;
import ch.invoiceforge.core.validation.ValidationIssue;
import ch.invoiceforge.export.InvoiceJsonExporter;
import ch.invoiceforge.pdf.InvoicePdfGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class InvoiceForgeMcpServer {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private InvoiceForgeMcpServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8088;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/tools", InvoiceForgeMcpServer::tools);
        server.createContext("/calculate_invoice_total", exchange -> withInvoice(exchange, invoice -> TotalCalculator.calculate(invoice)));
        server.createContext("/validate_invoice", InvoiceForgeMcpServer::validateInvoice);
        server.createContext("/export_invoice_json", InvoiceForgeMcpServer::exportInvoiceJson);
        server.createContext("/create_invoice_pdf", exchange -> createPdf(exchange, DocumentType.INVOICE));
        server.createContext("/create_quote_pdf", exchange -> createPdf(exchange, DocumentType.QUOTE));
        server.createContext("/create_receipt_pdf", exchange -> createPdf(exchange, DocumentType.RECEIPT));
        server.start();
        System.out.println("InvoiceForge MCP HTTP server listening on http://localhost:" + port);
    }

    private static void tools(HttpExchange exchange) throws IOException {
        writeJson(exchange, Map.of("tools", List.of(
                "create_invoice_pdf",
                "create_quote_pdf",
                "create_receipt_pdf",
                "calculate_invoice_total",
                "validate_invoice",
                "export_invoice_json"
        )));
    }

    private static void validateInvoice(HttpExchange exchange) throws IOException {
        withInvoice(exchange, invoice -> Map.of("valid", true, "issues", List.of()));
    }

    private static void exportInvoiceJson(HttpExchange exchange) throws IOException {
        withInvoice(exchange, InvoiceJsonExporter::toJson);
    }

    private static void createPdf(HttpExchange exchange, DocumentType documentType) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, Map.of("error", "POST required"));
            return;
        }
        JsonNode request = MAPPER.readTree(exchange.getRequestBody());
        Invoice invoice = MAPPER.treeToValue(request.get("invoice"), Invoice.class);
        invoice.setDocumentType(documentType);
        InvoiceValidator.requireValid(invoice);
        Path output = Path.of(request.path("outputPath").asText(documentType.name().toLowerCase() + ".pdf"));
        InvoicePdfGenerator.generate(invoice, output);
        writeJson(exchange, Map.of("outputPath", output.toString()));
    }

    private static void withInvoice(HttpExchange exchange, InvoiceHandler handler) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, Map.of("error", "POST required"));
            return;
        }
        Invoice invoice = MAPPER.readValue(exchange.getRequestBody(), Invoice.class);
        List<ValidationIssue> issues = InvoiceValidator.validate(invoice);
        if (!issues.isEmpty()) {
            writeJson(exchange, 400, Map.of("valid", false, "issues", issues));
            return;
        }
        try {
            writeJson(exchange, handler.handle(invoice));
        } catch (InvoiceValidationException ex) {
            writeJson(exchange, 400, Map.of("valid", false, "issues", ex.getIssues()));
        }
    }

    private static void writeJson(HttpExchange exchange, Object value) throws IOException {
        writeJson(exchange, 200, value);
    }

    private static void writeJson(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] body = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    @FunctionalInterface
    private interface InvoiceHandler {
        Object handle(Invoice invoice) throws IOException;
    }
}
