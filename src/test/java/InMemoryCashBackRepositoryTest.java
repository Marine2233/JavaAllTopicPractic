import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import testingJunit.CashBackResult;
import testingJunit.InMemoryCashBackRepository;
import testingJunit.Purchase;
import testingJunit.enums.CardType;
import testingJunit.enums.PurchaseCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryCashBackRepositoryTest {

    private InMemoryCashBackRepository repository;

    @BeforeEach
    public void setUp(){
        repository = new InMemoryCashBackRepository();
    }

    public Purchase createObject(String opId,long clId,BigDecimal amount){
        return new Purchase(opId,clId,amount,PurchaseCategory.SUPERMARKET,CardType.PREMIUM,LocalDate.now());
    }

    @Test
    public void findByIdPositiveScript(){

        Purchase purchase = createObject("x-101",1, BigDecimal.valueOf(1000));
        YearMonth month = YearMonth.from(purchase.purchasedDate());
        CashBackResult result = new CashBackResult(purchase,month,BigDecimal.valueOf(100));

        repository.save(result);
        CashBackResult find = repository.findByOperationId(result.purchase().operationId()).orElse(null);

        assertEquals(result,find);
    }

    @Test
    public void findByOptionalEmpty(){
//        Purchase purchase = createObject("x-101",1, BigDecimal.valueOf(1000));
//        YearMonth month = YearMonth.from(purchase.purchasedDate());
//        CashBackResult result = new CashBackResult(purchase,month,BigDecimal.valueOf(100));
//
//        repository.save(result);
        Optional<CashBackResult> result1 = repository.findByOperationId("bad_id");

        assertEquals(Optional.empty(),result1);
    }

    @Test
    public void repeatSaveById(){

        Purchase purchase = createObject("x-101",1, BigDecimal.valueOf(1000));
        YearMonth month = YearMonth.from(purchase.purchasedDate());
        CashBackResult result = new CashBackResult(purchase,month,BigDecimal.valueOf(100));

        repository.save(result);

        assertThrows(IllegalStateException.class,()->repository.save(result));
    }

    @Test
    public void doubleCashbackOnlyClientForMonth(){

        Purchase purchase = createObject("x-101",1, BigDecimal.valueOf(1000));
        Purchase purchase2 = createObject("x-102",1, BigDecimal.valueOf(1000));
        YearMonth month = YearMonth.from(purchase.purchasedDate());
        CashBackResult result = new CashBackResult(purchase,month,BigDecimal.valueOf(100));
        CashBackResult result2 = new CashBackResult(purchase2,month,BigDecimal.valueOf(100));

        repository.save(result);
        repository.save(result2);

        BigDecimal totalCash = result.cashbackAmount().add(result.cashbackAmount());
        totalCash = totalCash.setScale(2, RoundingMode.HALF_EVEN);

        assertEquals(new BigDecimal("200.00"),totalCash);
    }

    @Test
    public void cashbackByCurrentAndBeforeMonth() {
        Purchase purchaseCurrent = createObject("x-101", 1, BigDecimal.valueOf(1000));
        Purchase beforeMonthPurchase = new Purchase("x-111", 1, BigDecimal.valueOf(1800), PurchaseCategory.SUPERMARKET, CardType.PREMIUM, LocalDate.of(2026, 10, 10));
        YearMonth currentMonth = YearMonth.from(purchaseCurrent.purchasedDate());
        YearMonth beforeMonth = YearMonth.from(beforeMonthPurchase.purchasedDate());

        CashBackResult currentCash = new CashBackResult(purchaseCurrent, currentMonth, BigDecimal.valueOf(200.00));
        CashBackResult beforeCash = new CashBackResult(beforeMonthPurchase, beforeMonth, BigDecimal.valueOf(100.00));

        repository.save(currentCash);
        repository.save(beforeCash);

        CashBackResult result = repository.findByOperationId(currentCash.purchase().operationId()).orElse(null);

        assertEquals(result, currentCash);

    }

    @Test
    public void cashBackForTwoClients(){
        Purchase purchase1 = createObject("op-1",1,BigDecimal.valueOf(1200));
        Purchase purchase2 = createObject("op-2",2,BigDecimal.valueOf(800));
        YearMonth cli1 = YearMonth.from(purchase1.purchasedDate());
        YearMonth cli2 = YearMonth.from(purchase2.purchasedDate());

        CashBackResult result = new CashBackResult(purchase1,cli1,BigDecimal.valueOf(30));
        CashBackResult result2 = new CashBackResult(purchase2,cli2,BigDecimal.valueOf(100));

        repository.save(result);
        repository.save(result2);

        assertNotEquals(result2,result);
    }

    @Test
    public void emptyRepository(){

        BigDecimal total = repository.getTotalForMonth(2,YearMonth.now());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN), total);
    }
}
