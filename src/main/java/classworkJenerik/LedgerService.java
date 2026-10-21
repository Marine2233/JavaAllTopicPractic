package classworkJenerik;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class LedgerService {
    private final Repository<BigInteger,LedgerEntry>repository;

    public boolean post(LedgerEntry entry){
        if (entry == null){
            throw new NullPointerException("null");
        }
        if (repository.findById(entry.id()).isPresent()){
            return false;
        }

        if (entry.type() == EntryType.DEBIT && getBalance(entry.accountId()).compareTo(entry.amount()) < 0 ){
            throw new RuntimeException("Недостаточно ср-в.");
        }

        return repository.save(entry);
    }

    public BigDecimal getBalance(String accountId){
      return   repository.findAll().stream().
                filter(account-> account.accountId().equals(accountId)).
                map(entry -> entry.type() == EntryType.CREDIT ? entry.amount() : entry.amount().negate()).
                reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> getAllBalances(){
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(LedgerEntry::accountId,Collectors.reducing(
                        BigDecimal.ZERO,
                ledgerEntry ->
                ledgerEntry.type() == EntryType.CREDIT ? ledgerEntry.amount() : ledgerEntry.amount().negate(),
                        BigDecimal::add)));
    }

    public List<LedgerEntry> getHistory(String accountId){
        return repository.findAll().stream().filter(entry-> entry.accountId().equals(accountId)).
                sorted(Comparator.comparing(LedgerEntry::createdAt).thenComparing(LedgerEntry::id)).
                toList();
    }
    public long getOperationCount(String accountId){
        return repository.findAll().stream().filter(entry-> entry.accountId().equals(accountId)).count();
    }



}
