import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import testingJunit.CalculatorCacheBack;
import testingJunit.CashBackResult;
import testingJunit.CashBackService;
import testingJunit.Purchase;
import testingJunit.enums.CardType;
import testingJunit.enums.PurchaseCategory;
import testingJunit.interfaces.CashBackRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CashBackServiceTest {

    @Mock
    private CashBackRepository repository;

    @Mock
    private CalculatorCacheBack calculatorCashBack;

    @InjectMocks
    private CashBackService service;


//    @BeforeEach
//    public void setUp(){
//        service = new CashBackService(repository,calculatorCashBack);
//    }

    private Purchase createParametrizedPurchase(String opId, long clientId, BigDecimal amount) {
        return new Purchase(opId, clientId, amount, PurchaseCategory.SUPERMARKET, CardType.PREMIUM, LocalDate.of(2026, 5, 14));
    }

    @Test
    public void сheckThatTheLimitHasNotBeenReached() {
        Purchase testPurchase = createParametrizedPurchase("x-1",1,BigDecimal.valueOf(1000));
        YearMonth month = YearMonth.from(testPurchase.purchasedDate());

        when(calculatorCashBack.getMonthlyLimit(testPurchase.cardType())).thenReturn(BigDecimal.valueOf(2000));
        when(calculatorCashBack.calculate(testPurchase)).thenReturn(BigDecimal.valueOf(200));
        when(repository.findByOperationId(testPurchase.operationId())).thenReturn(Optional.empty());
        when(repository.getTotalForMonth(testPurchase.clientId(),month)).thenReturn(BigDecimal.valueOf(1000));

        CashBackResult cashBackResult = service.process(testPurchase);

        assertEquals(BigDecimal.valueOf(200).setScale(2,RoundingMode.HALF_EVEN),cashBackResult.cashbackAmount());
        assertNotNull(cashBackResult);
        verify(repository,times(1)).save(cashBackResult);
    }

    @Test
    @DisplayName("остаток кеш-бека 20р в текущем месяце")
    public void Limit20RubForThisMonth(){
        Purchase testData = createParametrizedPurchase("1",1,BigDecimal.valueOf(500));
        YearMonth testMonth = YearMonth.from(testData.purchasedDate());

        when(repository.getTotalForMonth(testData.clientId(),testMonth)).thenReturn(BigDecimal.valueOf(1980));
        when(repository.findByOperationId(testData.operationId())).thenReturn(Optional.empty());
        when(calculatorCashBack.calculate(testData)).thenReturn(BigDecimal.valueOf(50));
        when(calculatorCashBack.getMonthlyLimit(testData.cardType())).thenReturn(BigDecimal.valueOf(2000));

        CashBackResult testResult = service.process(testData);

        assertEquals(BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_EVEN), testResult.cashbackAmount());
        assertNotNull(testResult);

        verify(repository,times(1)).save(testResult);
    }

    @Test
    @DisplayName("исчерпан лимит остаток кеш-бека в текущем месяце-0")
    public void zeroLimitThisMonth(){
        Purchase purchase = createParametrizedPurchase("x-1",1,BigDecimal.valueOf(1500));
        YearMonth month = YearMonth.from(purchase.purchasedDate());

        when(repository.getTotalForMonth(purchase.clientId(),month)).thenReturn(BigDecimal.valueOf(2000));
        when(repository.findByOperationId(purchase.operationId())).thenReturn(Optional.empty());
        when(calculatorCashBack.getMonthlyLimit(purchase.cardType())).thenReturn(BigDecimal.valueOf(2000));
        when(calculatorCashBack.calculate(purchase)).thenReturn(BigDecimal.valueOf(200));

        CashBackResult cashBackResult = service.process(purchase);
        assertNotNull(cashBackResult);
        assertEquals(BigDecimal.ZERO.setScale(2,RoundingMode.HALF_EVEN),cashBackResult.cashbackAmount());

        verify(repository,times(1)).save(cashBackResult);
    }

    @Test
    @DisplayName("предыдущих начислений нет.")
    public void notBeforeCashback(){
        Purchase purchase = createParametrizedPurchase("x-1",1,BigDecimal.valueOf(2000));
        YearMonth month = YearMonth.from(purchase.purchasedDate());

        when(calculatorCashBack.calculate(purchase)).thenReturn(new BigDecimal("200.00"));
        when(repository.getTotalForMonth(purchase.clientId(),month)).thenReturn(BigDecimal.ZERO);
        when(repository.findByOperationId(purchase.operationId())).thenReturn(Optional.empty());
        when(calculatorCashBack.getMonthlyLimit(purchase.cardType())).thenReturn(BigDecimal.valueOf(2000));

        CashBackResult cashBackResult = service.process(purchase);

        assertNotNull(cashBackResult);
        assertEquals(new BigDecimal("200.00"),cashBackResult.cashbackAmount());

        verify(repository,times(1)).save(cashBackResult);
    }

    @Test
    @DisplayName("проверка дубликатов ")
    public void repeatIdentityPurchase(){

        Purchase purchase = createParametrizedPurchase("x-1",1,BigDecimal.valueOf(1800));
        YearMonth month = YearMonth.from(purchase.purchasedDate());
        CashBackResult inDB = new CashBackResult(purchase,month,BigDecimal.valueOf(180));

        when(repository.findByOperationId(purchase.operationId())).thenReturn(Optional.of(inDB));

        CashBackResult cashBackResult = service.process(purchase);
        assertNotNull(cashBackResult);
        assertSame(inDB,cashBackResult);
        verify(repository,never()).save(any());
    }

    @Test
    @DisplayName("проверка выброса исключения. причина: разная сумма и одинаковый id")
    public void repeatIdWithOtherAmount(){
        Purchase origin = createParametrizedPurchase("x-1",1,BigDecimal.valueOf(5000));
        Purchase repeat = createParametrizedPurchase("x-1",1,BigDecimal.valueOf(3000));
        YearMonth month = YearMonth.from(origin.purchasedDate());

        CashBackResult result = new CashBackResult(origin,month,BigDecimal.valueOf(100));
        when(repository.findByOperationId(repeat.operationId())).thenReturn(Optional.of(result));

        assertThrows(IllegalArgumentException.class, () -> service.process(repeat), "несоответствие полей.");
    }

    @Test
    @DisplayName("проверка выброса исключения. причина разные клиенты одинаковые id")
    public void repeatIdWithOtherClient(){
        Purchase origin = createParametrizedPurchase("x-1",1,BigDecimal.valueOf(3000));
        Purchase fake = createParametrizedPurchase("x-1",2,BigDecimal.valueOf(3000));
        YearMonth month = YearMonth.from(origin.purchasedDate());

        CashBackResult originInDB = new CashBackResult(origin,month,BigDecimal.valueOf(800));
        when(repository.findByOperationId(fake.operationId())).thenReturn(Optional.of(originInDB));

        assertThrows(IllegalArgumentException.class,()->service.process(fake));
    }

    @Test
    @DisplayName("проверка на исключение null")
    public void argNNull(){

        assertThrows(IllegalArgumentException.class,()->service.process(null));

    }
}
