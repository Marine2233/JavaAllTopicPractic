package classworkLessonRecordBIGINTDECPolymorphismOpenAndClosable;

import lombok.Getter;
import lombok.ToString;

import java.math.BigInteger;
import java.util.List;

@Getter
@ToString
public final class Invoice {

    private final BigInteger id;
    private final List<InvoiceLine>invoiceLines;
    private final Discount discount;

    public Invoice(BigInteger id,List<InvoiceLine>invoiceLines,Discount discount){

        if (id == null || id.compareTo(BigInteger.ZERO) <= 0 ){
            throw new RuntimeException("id не может быть отрицательным");
        }

        if (invoiceLines == null || invoiceLines.isEmpty()){
            throw new RuntimeException("Список не создан или пустой.");
        }

        if (discount == null ){
            throw new NullPointerException("Discount null Exception.");
        }

        this.id = id;
        this.invoiceLines = List.copyOf(invoiceLines);
        this.discount = discount;
    }

}
