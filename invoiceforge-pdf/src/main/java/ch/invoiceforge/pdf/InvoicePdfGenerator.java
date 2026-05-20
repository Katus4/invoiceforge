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
import ch.invoiceforge.core.validation.InvoiceValidator;
import net.codecrete.qrbill.generator.Bill;
import net.codecrete.qrbill.generator.BillFormat;
import net.codecrete.qrbill.generator.GraphicsFormat;
import net.codecrete.qrbill.generator.Language;
import net.codecrete.qrbill.generator.OutputSize;
import net.codecrete.qrbill.generator.QRBill;
import net.codecrete.qrbill.generator.ValidationMessage;
import net.codecrete.qrbill.generator.ValidationResult;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

public final class InvoicePdfGenerator {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final PDType1Font FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final Color INK = new Color(35, 39, 47);
    private static final Color MUTED = new Color(101, 112, 128);
    private static final Color RULE = new Color(214, 219, 226);
    private static final Color BAND = new Color(244, 247, 250);

    private InvoicePdfGenerator() {
    }

    public static void generate(Invoice invoice, String outputPath) throws IOException {
        generate(invoice, Path.of(outputPath));
    }

    public static void generate(Invoice invoice, Path outputPath) throws IOException {
        InvoiceValidator.requireValid(invoice);
        if (shouldAppendSwissQrBill(invoice)) {
            generateWithSwissQrBill(invoice, outputPath);
            return;
        }
        writeInvoicePages(invoice, outputPath);
    }

    private static void generateWithSwissQrBill(Invoice invoice, Path outputPath) throws IOException {
        Path tempDirectory = outputPath.toAbsolutePath().getParent();
        if (tempDirectory == null) {
            tempDirectory = Path.of(".");
        }
        Path invoicePdf = Files.createTempFile(tempDirectory, "invoiceforge-invoice-", ".pdf");
        Path qrBillPdf = Files.createTempFile(tempDirectory, "invoiceforge-qrbill-", ".pdf");
        try {
            Files.deleteIfExists(invoicePdf);
            writeInvoicePages(invoice, invoicePdf);
            Files.write(qrBillPdf, createSwissQrBill(invoice));

            PDFMergerUtility merger = new PDFMergerUtility();
            merger.addSource(invoicePdf.toFile());
            merger.addSource(qrBillPdf.toFile());
            merger.setDestinationFileName(outputPath.toString());
            merger.mergeDocuments(null);
        } finally {
            Files.deleteIfExists(invoicePdf);
            Files.deleteIfExists(qrBillPdf);
        }
    }

    private static void writeInvoicePages(Invoice invoice, Path outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                PdfCursor cursor = new PdfCursor(content, page);
                drawHeader(document, invoice, cursor);
                drawParties(invoice, cursor);
                drawItems(invoice, cursor);
                drawTotals(invoice, cursor);
                drawPaymentInfo(invoice, cursor);
                drawFooter(invoice, cursor);
            }

            document.save(outputPath.toFile());
        }
    }

    private static void drawHeader(PDDocument document, Invoice invoice, PdfCursor cursor) throws IOException {
        Company company = invoice.getCompany();
        cursor.fillRect(0, cursor.pageHeight - 116, cursor.pageWidth, 116, BAND);

        String logoPath = company == null ? null : company.getLogoPath();
        if (logoPath != null && Files.exists(Path.of(logoPath))) {
            PDImageXObject image = PDImageXObject.createFromFile(logoPath, document);
            cursor.content.drawImage(image, cursor.left, cursor.pageHeight - 88, 96, 48);
        }

        String title = switch (invoice.getDocumentType()) {
            case INVOICE -> "Rechnung";
            case QUOTE -> "Offerte";
            case RECEIPT -> "Quittung";
        };
        cursor.text(title, cursor.left, cursor.y, 26, BOLD, INK);
        cursor.text(nullToDash(invoice.getInvoiceNumber()), cursor.left, cursor.y - 22, 10, FONT, MUTED);

        float x = cursor.right - 190;
        cursor.text("Datum", x, cursor.y, 9, BOLD, MUTED);
        cursor.text(invoice.getInvoiceDate().format(DATE_FORMAT), x + 78, cursor.y, 9, FONT, INK);
        float metaY = cursor.y - 15;
        if (invoice.getDocumentType() == DocumentType.INVOICE && invoice.getDueDate() != null) {
            cursor.text("Faellig", x, metaY, 9, BOLD, MUTED);
            cursor.text(invoice.getDueDate().format(DATE_FORMAT), x + 78, metaY, 9, FONT, INK);
            metaY -= 15;
        } else if (invoice.getDocumentType() == DocumentType.QUOTE && invoice.getDueDate() != null) {
            cursor.text("Gueltig bis", x, metaY, 9, BOLD, MUTED);
            cursor.text(invoice.getDueDate().format(DATE_FORMAT), x + 78, metaY, 9, FONT, INK);
            metaY -= 15;
        }
        cursor.text("Waehrung", x, metaY, 9, BOLD, MUTED);
        cursor.text(invoice.getCurrency(), x + 78, metaY, 9, FONT, INK);

        cursor.y -= 86;
    }

    private static void drawParties(Invoice invoice, PdfCursor cursor) throws IOException {
        float top = cursor.y;
        float mid = cursor.left + 255;

        cursor.text("Von", cursor.left, top, 9, BOLD, MUTED);
        writeParty(invoice.getCompany(), cursor.left, top - 18, cursor);

        cursor.text("An", mid, top, 9, BOLD, MUTED);
        writeParty(invoice.getCustomer(), mid, top - 18, cursor);

        cursor.y -= 112;
    }

    private static void drawItems(Invoice invoice, PdfCursor cursor) throws IOException {
        float[] columns = {cursor.left, cursor.left + 230, cursor.left + 286, cursor.left + 350, cursor.left + 408, cursor.left + 472};
        cursor.line(cursor.left, cursor.y, cursor.right, cursor.y, RULE);
        cursor.y -= 17;
        cursor.text("Position", columns[0], cursor.y, 9, BOLD, MUTED);
        cursor.text("Menge", columns[1], cursor.y, 9, BOLD, MUTED);
        cursor.text("Preis", columns[2], cursor.y, 9, BOLD, MUTED);
        cursor.text("Rabatt", columns[3], cursor.y, 9, BOLD, MUTED);
        cursor.text("MWST", columns[4], cursor.y, 9, BOLD, MUTED);
        cursor.text("Total", columns[5], cursor.y, 9, BOLD, MUTED);
        cursor.y -= 9;
        cursor.line(cursor.left, cursor.y, cursor.right, cursor.y, RULE);
        cursor.y -= 17;

        for (InvoiceItem item : invoice.getItems()) {
            InvoiceItemTotals totals = TotalCalculator.calculateItem(item);
            cursor.text(clip(item.getDescription(), 42), columns[0], cursor.y, 9, FONT, INK);
            cursor.text(item.getQuantity().toPlainString(), columns[1], cursor.y, 9, FONT, INK);
            cursor.text(formatMoney(item.getUnitPrice(), invoice.getCurrency()), columns[2], cursor.y, 9, FONT, INK);
            cursor.text(item.getDiscountRate().toPlainString() + "%", columns[3], cursor.y, 9, FONT, INK);
            cursor.text(item.getVatRate().toPlainString() + "%", columns[4], cursor.y, 9, FONT, INK);
            cursor.text(formatMoney(totals.grossAmount(), invoice.getCurrency()), columns[5], cursor.y, 9, FONT, INK);
            cursor.y -= 18;
        }
        cursor.y -= 6;
        cursor.line(cursor.left, cursor.y, cursor.right, cursor.y, RULE);
        cursor.y -= 22;
    }

    private static void drawTotals(Invoice invoice, PdfCursor cursor) throws IOException {
        InvoiceTotals totals = TotalCalculator.calculate(invoice);
        float x = cursor.right - 200;
        cursor.text("Netto-Total", x, cursor.y, 10, FONT, MUTED);
        cursor.text(formatMoney(totals.netTotal(), invoice.getCurrency()), x + 105, cursor.y, 10, FONT, INK);
        cursor.y -= 16;
        cursor.text("MWST", x, cursor.y, 10, FONT, MUTED);
        cursor.text(formatMoney(totals.vatTotal(), invoice.getCurrency()), x + 105, cursor.y, 10, FONT, INK);
        cursor.y -= 20;
        cursor.text("Brutto-Total", x, cursor.y, 13, BOLD, INK);
        cursor.text(formatMoney(totals.grossTotal(), invoice.getCurrency()), x + 105, cursor.y, 13, BOLD, INK);
        cursor.y -= 42;
    }

    private static void drawPaymentInfo(Invoice invoice, PdfCursor cursor) throws IOException {
        if (invoice.getDocumentType() != DocumentType.INVOICE) {
            if (invoice.getNotes() != null) {
                cursor.text(invoice.getNotes(), cursor.left, cursor.y, 10, FONT, INK);
            }
            return;
        }
        Company company = invoice.getCompany();
        cursor.text("Zahlungsinformationen", cursor.left, cursor.y, 11, BOLD, INK);
        cursor.y -= 16;
        if (company.getIban() != null) {
            cursor.text("IBAN: " + company.getIban(), cursor.left, cursor.y, 10, FONT, INK);
            cursor.y -= 14;
        }
        if (company.getPaymentTerms() != null) {
            cursor.text(company.getPaymentTerms(), cursor.left, cursor.y, 10, FONT, INK);
            cursor.y -= 14;
        }
        if (shouldAppendSwissQrBill(invoice)) {
            cursor.text("Swiss QR-Bill payment part follows on the next page.", cursor.left, cursor.y, 9, FONT, MUTED);
            cursor.y -= 14;
        }
        if (invoice.getNotes() != null) {
            cursor.text(invoice.getNotes(), cursor.left, cursor.y, 10, FONT, INK);
        }
    }

    private static void drawFooter(Invoice invoice, PdfCursor cursor) throws IOException {
        Company company = invoice.getCompany();
        String footer = company.getName();
        if (company.getEmail() != null) {
            footer += " | " + company.getEmail();
        }
        cursor.line(cursor.left, 42, cursor.right, 42, RULE);
        cursor.text(footer, cursor.left, 27, 8, FONT, MUTED);
    }

    private static byte[] createSwissQrBill(Invoice invoice) throws IOException {
        Bill bill = new Bill();
        bill.setAccount(invoice.getCompany().getIban().replace(" ", ""));
        bill.setAmount(TotalCalculator.calculate(invoice).grossTotal());
        bill.setCurrency(invoice.getCurrency());
        bill.setCreditor(toQrAddress(invoice.getCompany().getName(), invoice.getCompany().getAddress()));
        bill.setDebtor(toQrAddress(invoice.getCustomer().getName(), invoice.getCustomer().getAddress()));
        bill.setReferenceType(Bill.REFERENCE_TYPE_NO_REF);
        bill.setUnstructuredMessage("Invoice " + invoice.getInvoiceNumber());

        BillFormat format = new BillFormat();
        format.setGraphicsFormat(GraphicsFormat.PDF);
        format.setOutputSize(OutputSize.A4_PORTRAIT_SHEET);
        format.setLanguage(Language.DE);
        bill.setFormat(format);

        ValidationResult validation = QRBill.validate(bill);
        if (!validation.isValid()) {
            String messages = validation.getValidationMessages().stream()
                    .map(InvoicePdfGenerator::formatQrValidationMessage)
                    .collect(Collectors.joining("; "));
            throw new IOException("Swiss QR bill validation failed: " + messages);
        }
        return QRBill.generate(bill);
    }

    private static net.codecrete.qrbill.generator.Address toQrAddress(String name, Address source) {
        net.codecrete.qrbill.generator.Address target = new net.codecrete.qrbill.generator.Address();
        target.setName(name);
        StreetParts streetParts = splitStreet(source.getStreet());
        target.setStreet(streetParts.street());
        target.setHouseNo(streetParts.houseNo());
        target.setPostalCode(source.getPostalCode());
        target.setTown(source.getCity());
        target.setCountryCode(source.getCountry());
        return target;
    }

    private static boolean shouldAppendSwissQrBill(Invoice invoice) {
        if (invoice.getDocumentType() != DocumentType.INVOICE) {
            return false;
        }
        Company company = invoice.getCompany();
        if (company == null || company.getIban() == null || company.getIban().isBlank()) {
            return false;
        }
        String currency = invoice.getCurrency();
        return "CHF".equals(currency) || "EUR".equals(currency);
    }

    private static void writeParty(Company company, float x, float y, PdfCursor cursor) throws IOException {
        if (company == null) {
            return;
        }
        cursor.text(company.getName(), x, y, 11, BOLD, INK);
        writeAddress(company.getAddress(), x, y - 15, cursor);
        if (company.getEmail() != null) {
            cursor.text(company.getEmail(), x, y - 57, 9, FONT, MUTED);
        }
        if (company.getPhone() != null) {
            cursor.text(company.getPhone(), x, y - 70, 9, FONT, MUTED);
        }
    }

    private static void writeParty(Customer customer, float x, float y, PdfCursor cursor) throws IOException {
        if (customer == null) {
            return;
        }
        cursor.text(customer.getName(), x, y, 11, BOLD, INK);
        writeAddress(customer.getAddress(), x, y - 15, cursor);
        if (customer.getEmail() != null) {
            cursor.text(customer.getEmail(), x, y - 57, 9, FONT, MUTED);
        }
        if (customer.getPhone() != null) {
            cursor.text(customer.getPhone(), x, y - 70, 9, FONT, MUTED);
        }
    }

    private static void writeAddress(Address address, float x, float y, PdfCursor cursor) throws IOException {
        if (address == null) {
            return;
        }
        cursor.text(nullToDash(address.getStreet()), x, y, 10, FONT, INK);
        cursor.text((nullToDash(address.getPostalCode()) + " " + nullToDash(address.getCity())).trim(), x, y - 14, 10, FONT, INK);
        cursor.text(nullToDash(address.getCountry()), x, y - 28, 10, FONT, INK);
    }

    private static StreetParts splitStreet(String street) {
        String trimmed = street == null ? "" : street.trim();
        int lastSpace = trimmed.lastIndexOf(' ');
        if (lastSpace > 0 && trimmed.substring(lastSpace + 1).matches("\\d+[a-zA-Z]?(?:/\\d+)?")) {
            return new StreetParts(trimmed.substring(0, lastSpace), trimmed.substring(lastSpace + 1));
        }
        return new StreetParts(trimmed, null);
    }

    private static String formatQrValidationMessage(ValidationMessage message) {
        return message.getField() + " " + message.getMessageKey();
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

    private record StreetParts(String street, String houseNo) {
    }

    private static final class PdfCursor {
        private final PDPageContentStream content;
        private final float left = 50;
        private final float right;
        private final float pageWidth;
        private final float pageHeight;
        private float y;

        private PdfCursor(PDPageContentStream content, PDPage page) {
            this.content = content;
            this.pageWidth = page.getMediaBox().getWidth();
            this.pageHeight = page.getMediaBox().getHeight();
            this.right = pageWidth - 50;
            this.y = pageHeight - 55;
        }

        private void text(String value, float x, float y, float size, PDType1Font font, Color color) throws IOException {
            content.setNonStrokingColor(color);
            content.beginText();
            content.setFont(font, size);
            content.newLineAtOffset(x, y);
            content.showText(value == null ? "" : value);
            content.endText();
        }

        private void line(float x1, float y1, float x2, float y2, Color color) throws IOException {
            content.setStrokingColor(color);
            content.moveTo(x1, y1);
            content.lineTo(x2, y2);
            content.stroke();
        }

        private void fillRect(float x, float y, float width, float height, Color color) throws IOException {
            content.setNonStrokingColor(color);
            content.addRect(x, y, width, height);
            content.fill();
        }
    }
}
