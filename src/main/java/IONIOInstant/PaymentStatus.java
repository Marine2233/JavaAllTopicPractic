package IONIOInstant;
/**
 * PENDING
 * Запланированный момент ещё не наступил
 * DUE
 * Время платежа наступило, но прошло не больше 48 часов
 * OVERDUE
 * С назначенного момента прошло более 48 часов*/
public enum PaymentStatus {
    PENDING,
    DUE,
    OVERDUE

}
