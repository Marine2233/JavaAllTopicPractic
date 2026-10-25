package IONIOInstant;

import java.util.List;

public record ImportResult (List<PaymentOrder> accepted, List<ImportError> rejected){
    public ImportResult{
        List<PaymentOrder> orders = List.copyOf(accepted);
        List<ImportError> errors = List.copyOf(rejected);
    }
}
