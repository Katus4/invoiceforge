package ch.invoiceforge.core.model;

import ch.invoiceforge.core.util.Money;

import java.math.BigDecimal;

public class InvoiceItem {
    private String description;
    private BigDecimal quantity = BigDecimal.ONE;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal vatRate = BigDecimal.ZERO;
    private BigDecimal discountRate = BigDecimal.ZERO;

    public InvoiceItem() {
    }

    public InvoiceItem(String description, double quantity, double unitPrice, double vatRate) {
        this.description = description;
        this.quantity = Money.decimal(quantity);
        this.unitPrice = Money.decimal(unitPrice);
        this.vatRate = Money.decimal(vatRate);
    }

    public String getDescription() {
        return description;
    }

    public InvoiceItem setDescription(String description) {
        this.description = description;
        return this;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public InvoiceItem setQuantity(BigDecimal quantity) {
        this.quantity = Money.requireNonNegative(quantity, "quantity");
        return this;
    }

    public InvoiceItem setQuantity(double quantity) {
        return setQuantity(Money.decimal(quantity));
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public InvoiceItem setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = Money.requireNonNegative(unitPrice, "unitPrice");
        return this;
    }

    public InvoiceItem setUnitPrice(double unitPrice) {
        return setUnitPrice(Money.decimal(unitPrice));
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public InvoiceItem setVatRate(BigDecimal vatRate) {
        this.vatRate = Money.requireNonNegative(vatRate, "vatRate");
        return this;
    }

    public InvoiceItem setVatRate(double vatRate) {
        return setVatRate(Money.decimal(vatRate));
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public InvoiceItem setDiscountRate(BigDecimal discountRate) {
        this.discountRate = Money.requireNonNegative(discountRate, "discountRate");
        return this;
    }

    public InvoiceItem setDiscountRate(double discountRate) {
        return setDiscountRate(Money.decimal(discountRate));
    }
}
