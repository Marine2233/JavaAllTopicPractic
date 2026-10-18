package classworkLessonRecordBIGINTDECPolymorphismOpenAndClosable;

import java.math.BigDecimal;

public sealed interface Discount permits PercentDiscount,FixedDiscount {
    /**
     * @param subtotal - принимает стоимость
     * @return  - размер скидки, а не сумму после скидки*/
    public BigDecimal calculate(BigDecimal subtotal);
}
