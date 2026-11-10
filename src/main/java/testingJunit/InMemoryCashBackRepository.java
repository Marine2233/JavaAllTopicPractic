package testingJunit;

import testingJunit.interfaces.CashBackRepository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryCashBackRepository implements CashBackRepository {

    private final Map<String, CashBackResult> results = new HashMap<>();

    @Override
    public Optional<CashBackResult> findByOperationId(String operationId) {
        return results.values().stream().filter(cash-> cash.purchase().operationId().equals(operationId)).findAny();
    }

    @Override
    public BigDecimal getTotalForMonth(long clientId, YearMonth month) {
        return results.values().stream().
                filter(purchase-> purchase.purchase().clientId() == clientId && purchase.month().equals(month)).
                map(CashBackResult::cashbackAmount).
                reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    @Override
    public void save(CashBackResult result) {

        if (results.putIfAbsent(result.purchase().operationId(),result) != null) {
            throw new IllegalStateException("Результат с таким id уже существует");
        }
    }
}
