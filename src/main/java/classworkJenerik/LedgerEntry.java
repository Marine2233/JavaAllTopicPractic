package classworkJenerik;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

public record LedgerEntry(BigInteger id, String accountId, EntryType type, BigDecimal amount, Instant createdAt) implements Identifiable<BigInteger> {

    public LedgerEntry{
        if (id.compareTo(BigInteger.ZERO) <= 0 ){
            throw new NullPointerException("Невалидные данные Id");
        }

        if (accountId == null){
            throw new NullPointerException("Невалидные данные.");
        }

        if (type == null){
            throw new NullPointerException("Невалидные null.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0 ){
            throw new NullPointerException("Невалидные данные.");
        }

        if (createdAt == null){
            throw new NullPointerException("Невалидные данные.");
        }

    }

    @Override
    public BigInteger getId() {
        return id;
    }
}
