package testingJunit;

import testingJunit.enums.CardType;
import testingJunit.enums.PurchaseCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record Purchase(
        String operationId,
        long clientId,
        BigDecimal amount,
        PurchaseCategory category,
        CardType cardType,
        LocalDate purchasedDate
) {

    public Purchase{
        if (operationId == null || operationId.isEmpty()){
            throw new IllegalArgumentException("Идентификатор не может быть Null");
        }

        if (clientId <= 0){
            throw new IllegalArgumentException("Id должен быть строго положительный.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Сумма покупки должна быть больше 0.");
        }

        if (category == null || cardType == null){
            throw new IllegalArgumentException("Параметры категорий не могут быть null.");
        }

        if (purchasedDate == null){
            throw new IllegalArgumentException("Дата должна быть задана.");
        }

        try {
            amount = amount.setScale(2, RoundingMode.UNNECESSARY);
        }catch (ArithmeticException e){
            throw new IllegalArgumentException("числа после занятой не больше 2");
        }
    }
}
