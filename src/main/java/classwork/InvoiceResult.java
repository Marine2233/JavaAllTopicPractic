package classwork;

import java.math.BigDecimal;
import java.math.BigInteger;

public record InvoiceResult(BigInteger invoiceId,
                            BigDecimal subtotal,
                            BigDecimal discountAmount,
                            BigDecimal taxableBase,
                            BigDecimal tax,
                            BigDecimal total) {
    public InvoiceResult{
        if (invoiceId == null) {
            throw new NullPointerException("ID инвойса не может быть null.");
        }
        if (invoiceId.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным.");
        }

        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Промежуточная стоимость не может быть null или отрицательной.");
        }

        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Размер скидки не может быть null или отрицательным.");
        }

        if (taxableBase == null || taxableBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Налогооблагаемая база не может быть null или отрицательной.");
        }

        if (tax == null || tax.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Налог не может быть null или отрицательным.");
        }

        if (total == null || total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Итоговая сумма не может быть null или отрицательной.");
        }

    }
}
