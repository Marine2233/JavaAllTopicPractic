package testingJunit;
import testingJunit.interfaces.CashBackRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

public class CashBackService {

    private final CalculatorCacheBack calculator;
    private final CashBackRepository repository;

    public CashBackService(CashBackRepository repository, CalculatorCacheBack calculator){
        if (repository == null || calculator == null){
            throw new NullPointerException("Аргументы не могут быть null.");
        }
        this.calculator = calculator;
        this.repository = repository;

    }

    public CashBackResult process(Purchase purchase){

        if (purchase == null) throw new IllegalArgumentException("Аргумент не может быть null");

        CashBackResult result = repository.findByOperationId(purchase.operationId()).orElse(null);

        if (result != null && result.purchase().equals(purchase)){
            return result;

        }else if (result != null && result.purchase().operationId().equals(purchase.operationId()) && !purchase.equals(result.purchase())){
            throw new IllegalArgumentException("несоответствие полей.");
        }

        BigDecimal cashback = calculator.calculate(purchase);

        LocalDate date = purchase.purchasedDate();
        YearMonth yearMonth = YearMonth.from(date);

        BigDecimal monthlyLimit = calculator.getMonthlyLimit(purchase.cardType());
        BigDecimal alreadyEarned = repository.getTotalForMonth(purchase.clientId(), yearMonth);

        BigDecimal currentLimit = monthlyLimit.subtract(alreadyEarned);
        BigDecimal resultLimit = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

        if (currentLimit.compareTo(BigDecimal.ZERO) > 0) {
            resultLimit = cashback.min(currentLimit).setScale(2, RoundingMode.HALF_EVEN);
        }

        CashBackResult newResult = new CashBackResult(purchase,yearMonth,resultLimit);
        repository.save(newResult);
        return newResult;
    }

}
