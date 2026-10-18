package classwork;

import java.math.BigDecimal;

public record Product(String sku, String name, BigDecimal amount) {
    public Product{
        if (sku == null ||sku.isBlank()){
            throw new NullPointerException("Артикул не может быть null.");
        }
        if (name == null || name.isBlank()){
            throw new NullPointerException("Name не может быть null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0 ){
            throw new NullPointerException("Amount не может быть null");
        }
        BigDecimal price = new BigDecimal(amount.longValue());
        price.setScale(2);
    }
}
