package ch.invoiceforge.core.model;

import ch.invoiceforge.core.calc.TotalCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Invoice {
    private DocumentType documentType = DocumentType.INVOICE;
    private String invoiceNumber;
    private LocalDate invoiceDate = LocalDate.now();
    private LocalDate dueDate;
    private Customer customer;
    private Company company;
    private final List<InvoiceItem> items = new ArrayList<>();
    private String currency = "CHF";
    private String notes;

    public DocumentType getDocumentType() {
        return documentType;
    }

    public Invoice setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
        return this;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public Invoice setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
        return this;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public Invoice setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
        return this;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Invoice setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public Invoice setDueInDays(int days) {
        this.dueDate = invoiceDate.plusDays(days);
        return this;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Invoice setCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public Company getCompany() {
        return company;
    }

    public Invoice setCompany(Company company) {
        this.company = company;
        return this;
    }

    public List<InvoiceItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Invoice setItems(List<InvoiceItem> items) {
        this.items.clear();
        if (items != null) {
            this.items.addAll(items);
        }
        return this;
    }

    public Invoice addItem(InvoiceItem item) {
        this.items.add(item);
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public Invoice setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public Invoice setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public BigDecimal getNetTotal() {
        return TotalCalculator.calculate(this).netTotal();
    }

    public BigDecimal getVatTotal() {
        return TotalCalculator.calculate(this).vatTotal();
    }

    public BigDecimal getGrossTotal() {
        return TotalCalculator.calculate(this).grossTotal();
    }
}
