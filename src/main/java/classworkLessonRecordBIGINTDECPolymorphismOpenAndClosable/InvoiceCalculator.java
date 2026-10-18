package classworkLessonRecordBIGINTDECPolymorphismOpenAndClosable;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InvoiceCalculator {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.20");

    public InvoiceResult calculate(Invoice invoice){

        if (invoice == null){
            throw new NullPointerException("Exception null invoice");
        }

        BigDecimal subtotal = calculateSubtotal(invoice);

        BigDecimal discountAmount = invoice.getDiscount().calculate(subtotal)
                .setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal taxableBase = subtotal.subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_EVEN);

        BigDecimal tax = calculateTax(taxableBase);

        BigDecimal total = taxableBase.add(tax)
                .setScale(2, RoundingMode.HALF_EVEN);

        return new InvoiceResult(
                invoice.getId(),
                subtotal,
                discountAmount,
                taxableBase,
                tax,
                total
        );

    }
    public BigDecimal calculateSubtotal(Invoice invoice) {
        if (invoice == null) {
            throw new NullPointerException("Invoice не может быть null.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (InvoiceLine line : invoice.getInvoiceLines()) {
            subtotal = subtotal.add(line.getLineTotal());
        }

        return subtotal.setScale(2, RoundingMode.HALF_EVEN);
    }    public BigDecimal calculateTax(BigDecimal taxableBase) {
        if (taxableBase == null) {
            throw new NullPointerException("Налогооблагаемая база не может быть null.");
        }

        return taxableBase.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_EVEN);
    }
}
