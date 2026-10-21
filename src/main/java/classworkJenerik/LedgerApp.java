package classworkJenerik;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LedgerApp {
    public static void main(String[] args) {
        Repository<BigInteger, LedgerEntry> repository = new InMemoryRepository();
        LedgerService service = new LedgerService(repository);

        service.post(new LedgerEntry(BigInteger.valueOf(1), "A-100", EntryType.CREDIT, new BigDecimal("1000.00"),Instant.now()));
        service.post(new LedgerEntry(BigInteger.valueOf(2), "A-100", EntryType.DEBIT, new BigDecimal("160.00"), Instant.now()));
        service.post(new LedgerEntry(BigInteger.valueOf(3), "A-100", EntryType.CREDIT, new BigDecimal("0.01"), Instant.now()));
        service.post(new LedgerEntry(BigInteger.valueOf(4), "B-200", EntryType.CREDIT, new BigDecimal("500.00"), Instant.now()));

        System.out.println("--- Balances ---");
        service.getAllBalances().forEach((account, balance) ->
                System.out.printf("%s = %.2f\n", account, balance)
        );

        System.out.println();
        try {
            service.post(new LedgerEntry(BigInteger.valueOf(5), "A-100", EntryType.DEBIT, new BigDecimal("800.00"), Instant.now()));
        } catch (IllegalStateException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
        System.out.printf("A-100 balance: %.2f\n", service.getBalance("A-100"));

        System.out.println("\n--- Duplicate ID simulation ---");
        LedgerEntry original = new LedgerEntry(BigInteger.valueOf(10), "C-300", EntryType.CREDIT, new BigDecimal("100.00"), Instant.now());
        LedgerEntry duplicate = new LedgerEntry(BigInteger.valueOf(10), "D-400", EntryType.CREDIT, new BigDecimal("200.00"), Instant.now());

        System.out.println("Save original: " + service.post(original));
        System.out.println("Save duplicate: " + service.post(duplicate));

        System.out.println("\n--- Generics experiment ---");
        InMemoryRepository testRepo = new InMemoryRepository();

        List<LedgerEntry> entriesToSave = List.of(
                new LedgerEntry(BigInteger.valueOf(100), "X-999", EntryType.CREDIT, new BigDecimal("10.00"), Instant.now()),
                new LedgerEntry(BigInteger.valueOf(100), "X-999", EntryType.CREDIT, new BigDecimal("20.00"), Instant.now()),
                new LedgerEntry(BigInteger.valueOf(200), "Y-888", EntryType.CREDIT, new BigDecimal("50.00"), Instant.now())
        );

        int savedCount = testRepo.saveAll(entriesToSave);
        System.out.println("Saved count: " + savedCount);

        List<LedgerEntry> ledgerList = new ArrayList<>();
        testRepo.copyTo(ledgerList);
        System.out.println("Ledger list size: " + ledgerList.size());

        List<Object> objectList = new ArrayList<>();
        testRepo.copyTo(objectList);
        System.out.println("Object list size: " + objectList.size());
    }

}
