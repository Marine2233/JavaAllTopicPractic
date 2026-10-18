package classworkLessonRecordBIGINTDECPolymorphismOpenAndClosable;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record PercentDiscount(BigDecimal percent) implements Discount {

    public PercentDiscount{
        if (percent == null){
            throw new NullPointerException("Процент - null.");
        }
        if (percent.compareTo(BigDecimal.ZERO) < 0|| percent.compareTo(new BigDecimal("100")) > 0){
            throw new IllegalArgumentException("Невалидный параметр.");
        }
    }

    @Override
    public BigDecimal calculate(BigDecimal subtotal) {
        if (subtotal == null){
            return BigDecimal.ZERO;
        }
        BigDecimal oneHundred = new BigDecimal("100");

        return subtotal.multiply(percent).divide(oneHundred,2, RoundingMode.HALF_EVEN);
    }
}
