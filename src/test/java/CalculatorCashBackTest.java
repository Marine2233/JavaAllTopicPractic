import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import testingJunit.CalculatorCacheBack;
import testingJunit.Purchase;
import testingJunit.enums.CardType;
import testingJunit.enums.PurchaseCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static junit.framework.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CalculatorCashBackTest {

    private final CalculatorCacheBack cacheBack = new CalculatorCacheBack();

    @Test
    @DisplayName("cashback for type standard")
    public void shouldCashbackCalculatorForStandardType() {

        Purchase purchase = new Purchase("x-001",
                1,
                BigDecimal.valueOf(1000.00),
                PurchaseCategory.OTHER,
                CardType.STANDARD,
                LocalDate.now());

        BigDecimal actual = cacheBack.calculate(purchase);
        actual.setScale(2, RoundingMode.HALF_EVEN);
        assertTrue(BigDecimal.valueOf(10.00).compareTo(actual) == 0);
    }

    @Test
    @DisplayName("Standard + supermarket")
    public void shouldCashBackCalculateForStandardAndSupermarket() {
        Purchase purchase = new Purchase("x-001",
                1,
                BigDecimal.valueOf(1000.00),
                PurchaseCategory.SUPERMARKET,
                CardType.STANDARD,
                LocalDate.now());

        BigDecimal actual = cacheBack.calculate(purchase);
        assertTrue(BigDecimal.valueOf(20).compareTo(actual) == 0);

    }

    @Test
    @DisplayName("standard and transport")
    public void shouldCashbackStandardAndTransport() {
        Purchase purchase = new Purchase("x-001",
                1,
                BigDecimal.valueOf(1000.00),
                PurchaseCategory.TRANSPORT,
                CardType.STANDARD,
                LocalDate.now());

        BigDecimal actual = cacheBack.calculate(purchase);
        assertTrue(BigDecimal.valueOf(15).compareTo(actual) == 0);
    }

    @Test
    @DisplayName("cash for premium and other")
    public void shouldCashbackForPremiumAndOther() {
        Purchase purchase = new Purchase("x-001",
                1,
                BigDecimal.valueOf(1000.00),
                PurchaseCategory.OTHER,
                CardType.PREMIUM,
                LocalDate.now());

        BigDecimal actual = cacheBack.calculate(purchase);
        assertTrue(BigDecimal.valueOf(20).compareTo(actual) == 0);

    }

    @Test
    @DisplayName("cash for premium and supermarket")
    public void shouldCashbackForPremiumSupermarket() {
        Purchase purchase = new Purchase("x-001",
                1,
                BigDecimal.valueOf(1000.00),
                PurchaseCategory.SUPERMARKET,
                CardType.PREMIUM,
                LocalDate.now());

        BigDecimal actual = cacheBack.calculate(purchase);
        assertTrue(BigDecimal.valueOf(30).compareTo(actual) == 0);
    }

    @Test
    @DisplayName("cash for premium and transport")
    public void shouldCashbackForPremiumTransport() {
        Purchase purchase = new Purchase("x-001",
                1,
                BigDecimal.valueOf(1000.00),
                PurchaseCategory.TRANSPORT,
                CardType.PREMIUM,
                LocalDate.now());

        BigDecimal actual = cacheBack.calculate(purchase);
        assertTrue(BigDecimal.valueOf(25).compareTo(actual) == 0);
    }

    @Test
    @DisplayName("standard and transport 3 money")
    public void shouldCashbackStandardAndTransport3money(){
        Purchase purchase = new Purchase("x-001",
                1,
                BigDecimal.valueOf(3.00),
                PurchaseCategory.TRANSPORT,
                CardType.STANDARD,
                LocalDate.now());

        BigDecimal actual = cacheBack.calculate(purchase);
        assertTrue(BigDecimal.valueOf(0.04).compareTo(actual) == 0);
    }

    @Test
    @DisplayName("type premium")
    public void shouldCardTypePremium(){
        Purchase purchase = new Purchase("x-001",
                1,
                BigDecimal.valueOf(3.00),
                PurchaseCategory.TRANSPORT,
                CardType.PREMIUM,
                LocalDate.now());

        assertTrue(purchase.cardType().equals(CardType.PREMIUM));
    }

    @Test
    @DisplayName("type standard")
    public void shouldCardTypeStandard(){
        Purchase purchase = new Purchase("x-001",
                1,
                BigDecimal.valueOf(3.00),
                PurchaseCategory.TRANSPORT,
                CardType.STANDARD,
                LocalDate.now());

        assertTrue(purchase.cardType().equals(CardType.STANDARD));
    }

    @Test
    @DisplayName("month limit for premium")
    public void shouldLimitForTypePremium(){
        Purchase purchase = new Purchase("x-001",
                1,
                BigDecimal.valueOf(3.00),
                PurchaseCategory.TRANSPORT,
                CardType.PREMIUM,
                LocalDate.now());

        BigDecimal limit = cacheBack.getMonthlyLimit(purchase.cardType());
        assertTrue(limit.compareTo(BigDecimal.valueOf(2000.00)) == 0);
    }

    @Test
    @DisplayName("limit for standard")
    public void shouldLimitForTypeStandard(){
        Purchase purchase = new Purchase("x-001",
                1,
                BigDecimal.valueOf(3.00),
                PurchaseCategory.TRANSPORT,
                CardType.STANDARD,
                LocalDate.now());

        BigDecimal limit = cacheBack.getMonthlyLimit(purchase.cardType());
        assertTrue(limit.compareTo(BigDecimal.valueOf(500.00)) == 0);
    }

    @Test
    @DisplayName("arg null for purchase")
    public void shouldTrowExceptionIllegalArgumentEXlNullArgForPurchase(){
        assertThrows(IllegalArgumentException.class,()->cacheBack.calculate(null) );
    }

    @Test
    @DisplayName("type null")
    public void shouldTrowIllegalArgExForTypeNull(){
        assertThrows(IllegalArgumentException.class,()->cacheBack.getMonthlyLimit(null));
    }
}
