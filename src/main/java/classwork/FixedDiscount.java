package classwork;

import java.math.BigDecimal;

public record FixedDiscount(BigDecimal discount) implements Discount {

    public FixedDiscount{
        if (discount == null){
            throw new IllegalArgumentException("Размер скидки не может быть отрицательным.");
        }

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Размер скидки не может быть отрицательным.");
        }
    }


    @Override
    public BigDecimal calculate(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0 ){
            return BigDecimal.ZERO;
        }

        if (subtotal.compareTo(discount) > 0){
            return discount;
        }

        return subtotal;
    }
}
