package ch.invoiceforge.core.model;

public class Receipt extends Invoice {
    public Receipt() {
        setDocumentType(DocumentType.RECEIPT);
    }
}
