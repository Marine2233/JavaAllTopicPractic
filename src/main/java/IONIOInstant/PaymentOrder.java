package IONIOInstant;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public record PaymentOrder(String id, String client, BigDecimal amount, Instant createAtUtc, ZonedDateTime scheduledAt) {

    public PaymentOrder{

    if ( id == null  || client == null || amount == null || createAtUtc == null || scheduledAt == null){
        throw new IllegalArgumentException("Невалидные данные.");
    }

    if (amount.compareTo(BigDecimal.ZERO) < 0){
        throw new IllegalArgumentException("Баланс не может быть отрицательный");
    }

    }

    public Instant sheduledInstant(){
        return createAtUtc;
    }

    public LocalDate scheduledDate(){
        return LocalDate.now();
    }

    public ZoneId clientZone(){
        return scheduledAt.getZone();
    }
}
