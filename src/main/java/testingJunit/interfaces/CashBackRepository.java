package testingJunit.interfaces;

import testingJunit.CashBackResult;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

public interface CashBackRepository {

    Optional<CashBackResult> findByOperationId(String operationId);

    BigDecimal getTotalForMonth(long clientId, YearMonth month);

    void save(CashBackResult result);

}
