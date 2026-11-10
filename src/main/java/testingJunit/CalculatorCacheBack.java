package testingJunit;

import testingJunit.enums.CardType;
import testingJunit.enums.PurchaseCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculatorCacheBack {

    public BigDecimal calculate(Purchase purchase){

        if (purchase == null) {
            throw new IllegalArgumentException("аргумент не может быть null");
        }

        BigDecimal rate = BigDecimal.ZERO;

        if (purchase.cardType().equals(CardType.STANDARD)) {
            rate = rate.add(BigDecimal.ONE);
        } else if (purchase.cardType().equals(CardType.PREMIUM)) {
            rate = rate.add(BigDecimal.valueOf(2));
        } else {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }

        if (purchase.category().equals(PurchaseCategory.SUPERMARKET)) {
            rate = rate.add(BigDecimal.ONE);
        } else if (purchase.category().equals(PurchaseCategory.TRANSPORT)) {
            rate = rate.add(BigDecimal.valueOf(0.5));
        }

        return purchase.amount()
                .multiply(rate)
                .divide(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    public BigDecimal getMonthlyLimit(CardType cardType){

        if (cardType == null){
            throw new IllegalArgumentException("аргумент не может быть null");
        }

        return cardType.equals(CardType.PREMIUM)? BigDecimal.valueOf(2000).setScale(2,RoundingMode.HALF_EVEN):
                BigDecimal.valueOf(500).setScale(2,RoundingMode.HALF_EVEN);
    }

}
