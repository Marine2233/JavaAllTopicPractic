package classworkLessonRecordBIGINTDECPolymorphismOpenAndClosable;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
@Getter
@ToString
public final class InvoiceLine{

    private final Product product;
    private final int  quinity;

    public InvoiceLine(Product product, int quinity){
        if (product == null){
            throw  new NullPointerException("Невалидные данные.");
        }
        if (quinity <= 0){
            throw new RuntimeException("Невалидные данные параметр отрицательный");
        }

        this.product = product;
        this.quinity = quinity;
    }

    public BigDecimal getLineTotal(){
        BigDecimal quantity = new BigDecimal(quinity);
        return product.amount().multiply(quantity).setScale(2,RoundingMode.HALF_UP);
    }
}
