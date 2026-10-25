package IONIOInstant;

import java.time.*;
import java.util.List;

public class PaymentScheduler {

    public PaymentStatus getStatus(PaymentOrder order, Instant now) {

        Instant scheduled = order.scheduledAt().toInstant();

        if (now.isBefore(scheduled)) {
            return PaymentStatus.PENDING;
        }

        Duration age = Duration.between(scheduled, now);

        if (age.toHours() <= 48) {
            return PaymentStatus.DUE;
        }

        return PaymentStatus.OVERDUE;
    }

    public Duration timeUntilPayment(PaymentOrder order, Instant now){

        return Duration.between(now,order.sheduledInstant());
    }

    public Period calendarWaitingPeriod(PaymentOrder order){

        LocalDate createDate = order.createAtUtc().atZone(order.clientZone()).toLocalDate();

        LocalDate scheduledDate = order.scheduledDate();

        return Period.between(scheduledDate,createDate);
    }

    public List<PaymentOrder>getDueOrders(List<PaymentOrder> orders, Instant now){

        return orders.stream().
                filter(order -> timeUntilPayment(order,now).isZero() || timeUntilPayment(order,now).isNegative()).
                toList();

    }

}
