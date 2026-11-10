package testingJunit;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CashBackResult(

        Purchase purchase,
        YearMonth month,
        BigDecimal cashbackAmount
) {
    public CashBackResult {
        if (purchase == null) {
            throw new NullPointerException("Объект покупки не может быть null.");
        }

        boolean matchMonth = month.equals(YearMonth.from(purchase.purchasedDate()));
        if (month == null  || !matchMonth) {
            throw new IllegalArgumentException("Невалидная дата.");
        }

        if (cashbackAmount == null || cashbackAmount.compareTo(BigDecimal.ZERO) < 0 ){
            throw new IllegalArgumentException("Кешбек не может быть null");
        }
    }
}
