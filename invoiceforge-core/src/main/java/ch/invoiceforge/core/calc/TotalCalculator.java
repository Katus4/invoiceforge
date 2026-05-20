package ch.invoiceforge.core.calc;

import ch.invoiceforge.core.model.Invoice;
import ch.invoiceforge.core.model.InvoiceItem;
import ch.invoiceforge.core.util.Money;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TotalCalculator {
    private TotalCalculator() {
    }

    public static InvoiceItemTotals calculateItem(InvoiceItem item) {
        BigDecimal subtotal = Money.round(item.getQuantity().multiply(item.getUnitPrice()));
        BigDecimal discount = DiscountCalculator.calculate(subtotal, item.getDiscountRate());
        BigDecimal net = Money.round(subtotal.subtract(discount));
        BigDecimal vat = VatCalculator.calculate(net, item.getVatRate());
        BigDecimal gross = Money.round(net.add(vat));
        return new InvoiceItemTotals(subtotal, discount, net, vat, gross);
    }

    public static InvoiceTotals calculate(Invoice invoice) {
        BigDecimal netTotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal vatTotal = BigDecimal.ZERO;
        Map<BigDecimal, BigDecimal> vatByRate = new LinkedHashMap<>();

        for (InvoiceItem item : invoice.getItems()) {
            InvoiceItemTotals totals = calculateItem(item);
            netTotal = netTotal.add(totals.netAmount());
            discountTotal = discountTotal.add(totals.discountAmount());
            vatTotal = vatTotal.add(totals.vatAmount());
            vatByRate.merge(item.getVatRate(), totals.vatAmount(), BigDecimal::add);
        }

        netTotal = Money.round(netTotal);
        discountTotal = Money.round(discountTotal);
        vatTotal = Money.round(vatTotal);
        BigDecimal grossTotal = Money.round(netTotal.add(vatTotal));

        vatByRate.replaceAll((rate, amount) -> Money.round(amount));
        return new InvoiceTotals(netTotal, discountTotal, vatTotal, grossTotal, Map.copyOf(vatByRate));
    }
}
