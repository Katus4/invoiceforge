package ch.invoiceforge.core.model;

public class Quote extends Invoice {
    public Quote() {
        setDocumentType(DocumentType.QUOTE);
    }
}
