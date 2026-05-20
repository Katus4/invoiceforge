package ch.invoiceforge.pdf;

import ch.invoiceforge.core.calc.InvoiceItemTotals;
import ch.invoiceforge.core.calc.InvoiceTotals;
import ch.invoiceforge.core.calc.TotalCalculator;
import ch.invoiceforge.core.model.Address;
import ch.invoiceforge.core.model.Company;
import ch.invoiceforge.core.model.Customer;
import ch.invoiceforge.core.model.DocumentType;
import ch.invoiceforge.core.model.Invoice;
import ch.invoiceforge.core.model.InvoiceItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class InvoicePdfGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final PDType1Font FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private InvoicePdfGenerator() {
    }

    public static void generate(Invoice invoice, String outputPath) throws IOException {
        generate(invoice, Path.of(outputPath));
    }

    public static void generate(Invoice invoice, Path outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                PdfCursor cursor = new PdfCursor(content, page);
                drawHeader(document, invoice, cursor);
                drawParties(invoice, cursor);
                drawMeta(invoice, cursor);
                drawItems(invoice, cursor);
                drawTotals(invoice, cursor);
                drawPaymentInfo(invoice, cursor);
            }

            document.save(outputPath.toFile());
        }
    }

    private static void drawHeader(PDDocument document, Invoice invoice, PdfCursor cursor) throws IOException {
        Company company = invoice.getCompany();
        String logoPath = company == null ? null : company.getLogoPath();
        if (logoPath != null && Files.exists(Path.of(logoPath))) {
            PDImageXObject image = PDImageXObject.createFromFile(logoPath, document);
            cursor.content.drawImage(image, cursor.left, cursor.y - 28, 96, 48);
        }

        String title = switch (invoice.getDocumentType()) {
            case INVOICE -> "Rechnung";
            case QUOTE -> "Offerte";
            case RECEIPT -> "Quittung";
        };
        cursor.text(title, cursor.right - 170, cursor.y, 24, BOLD);
        cursor.y -= 55;

        if (company != null) {
            cursor.text(company.getName(), cursor.left, cursor.y, 12, BOLD);
            cursor.y -= 15;
            writeAddress(company.getAddress(), cursor.left, cursor.y, cursor);
            cursor.y -= 30;
            if (company.getEmail() != null) {
                cursor.text(company.getEmail(), cursor.left, cursor.y, 9, FONT);
                cursor.y -= 12;
            }
            if (company.getPhone() != null) {
                cursor.text(company.getPhone(), cursor.left, cursor.y, 9, FONT);
            }
        }
        cursor.y -= 35;
    }

    private static void drawParties(Invoice invoice, PdfCursor cursor) throws IOException {
        Customer customer = invoice.getCustomer();
        cursor.text("Kunde", cursor.left, cursor.y, 11, BOLD);
        cursor.y -= 15;
        if (customer != null) {
            cursor.text(customer.getName(), cursor.left, cursor.y, 10, FONT);
            cursor.y -= 13;
            writeAddress(customer.getAddress(), cursor.left, cursor.y, cursor);
        }
        cursor.y -= 40;
    }

    private static void drawMeta(Invoice invoice, PdfCursor cursor) throws IOException {
        float x = cursor.right - 200;
        float top = cursor.y + 55;
        cursor.text("Nummer:", x, top, 10, BOLD);
        cursor.text(nullToDash(invoice.getInvoiceNumber()), x + 80, top, 10, FONT);
        cursor.text("Datum:", x, top - 15, 10, BOLD);
        cursor.text(invoice.getInvoiceDate() == null ? "-" : invoice.getInvoiceDate().format(DATE_FORMAT), x + 80, top - 15, 10, FONT);
        cursor.text("Faellig:", x, top - 30, 10, BOLD);
        cursor.text(invoice.getDueDate() == null ? "-" : invoice.getDueDate().format(DATE_FORMAT), x + 80, top - 30, 10, FONT);
    }

    private static void drawItems(Invoice invoice, PdfCursor cursor) throws IOException {
        float[] columns = {cursor.left, cursor.left + 215, cursor.left + 275, cursor.left + 345, cursor.left + 415, cursor.left + 490};
        cursor.line(cursor.left, cursor.y, cursor.right, cursor.y);
        cursor.y -= 15;
        cursor.text("Position", columns[0], cursor.y, 9, BOLD);
        cursor.text("Menge", columns[1], cursor.y, 9, BOLD);
        cursor.text("Preis", columns[2], cursor.y, 9, BOLD);
        cursor.text("Rabatt", columns[3], cursor.y, 9, BOLD);
        cursor.text("MWST", columns[4], cursor.y, 9, BOLD);
        cursor.text("Total", columns[5], cursor.y, 9, BOLD);
        cursor.y -= 8;
        cursor.line(cursor.left, cursor.y, cursor.right, cursor.y);
        cursor.y -= 15;

        for (InvoiceItem item : invoice.getItems()) {
            InvoiceItemTotals totals = TotalCalculator.calculateItem(item);
            cursor.text(clip(item.getDescription(), 38), columns[0], cursor.y, 9, FONT);
            cursor.text(item.getQuantity().toPlainString(), columns[1], cursor.y, 9, FONT);
            cursor.text(formatMoney(item.getUnitPrice(), invoice.getCurrency()), columns[2], cursor.y, 9, FONT);
            cursor.text(item.getDiscountRate().toPlainString() + "%", columns[3], cursor.y, 9, FONT);
            cursor.text(item.getVatRate().toPlainString() + "%", columns[4], cursor.y, 9, FONT);
            cursor.text(formatMoney(totals.grossAmount(), invoice.getCurrency()), columns[5], cursor.y, 9, FONT);
            cursor.y -= 16;
        }
        cursor.y -= 10;
        cursor.line(cursor.left, cursor.y, cursor.right, cursor.y);
        cursor.y -= 20;
    }

    private static void drawTotals(Invoice invoice, PdfCursor cursor) throws IOException {
        InvoiceTotals totals = TotalCalculator.calculate(invoice);
        float x = cursor.right - 180;
        cursor.text("Netto-Total", x, cursor.y, 10, FONT);
        cursor.text(formatMoney(totals.netTotal(), invoice.getCurrency()), x + 95, cursor.y, 10, FONT);
        cursor.y -= 15;
        cursor.text("MWST", x, cursor.y, 10, FONT);
        cursor.text(formatMoney(totals.vatTotal(), invoice.getCurrency()), x + 95, cursor.y, 10, FONT);
        cursor.y -= 18;
        cursor.text("Brutto-Total", x, cursor.y, 12, BOLD);
        cursor.text(formatMoney(totals.grossTotal(), invoice.getCurrency()), x + 95, cursor.y, 12, BOLD);
        cursor.y -= 35;
    }

    private static void drawPaymentInfo(Invoice invoice, PdfCursor cursor) throws IOException {
        Company company = invoice.getCompany();
        cursor.text("Zahlungsinformationen", cursor.left, cursor.y, 11, BOLD);
        cursor.y -= 15;
        if (company != null && company.getIban() != null) {
            cursor.text("IBAN: " + company.getIban(), cursor.left, cursor.y, 10, FONT);
            cursor.y -= 13;
        }
        if (company != null && company.getPaymentTerms() != null) {
            cursor.text(company.getPaymentTerms(), cursor.left, cursor.y, 10, FONT);
            cursor.y -= 13;
        }
        if (invoice.getNotes() != null) {
            cursor.text(invoice.getNotes(), cursor.left, cursor.y, 10, FONT);
        }
    }

    private static void writeAddress(Address address, float x, float y, PdfCursor cursor) throws IOException {
        if (address == null) {
            return;
        }
        cursor.text(nullToDash(address.getStreet()), x, y, 10, FONT);
        cursor.text((nullToDash(address.getPostalCode()) + " " + nullToDash(address.getCity())).trim(), x, y - 13, 10, FONT);
        if (address.getCountry() != null) {
            cursor.text(address.getCountry(), x, y - 26, 10, FONT);
        }
    }

    private static String formatMoney(BigDecimal amount, String currency) {
        return String.format(Locale.ROOT, "%s %s", amount.toPlainString(), currency == null ? "" : currency).trim();
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String clip(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max - 1) + ".";
    }

    private static final class PdfCursor {
        private final PDPageContentStream content;
        private final float left = 50;
        private final float right;
        private float y;

        private PdfCursor(PDPageContentStream content, PDPage page) {
            this.content = content;
            this.right = page.getMediaBox().getWidth() - 50;
            this.y = page.getMediaBox().getHeight() - 55;
        }

        private void text(String value, float x, float y, float size, PDType1Font font) throws IOException {
            content.beginText();
            content.setFont(font, size);
            content.newLineAtOffset(x, y);
            content.showText(value == null ? "" : value);
            content.endText();
        }

        private void line(float x1, float y1, float x2, float y2) throws IOException {
            content.moveTo(x1, y1);
            content.lineTo(x2, y2);
            content.stroke();
        }
    }
}
