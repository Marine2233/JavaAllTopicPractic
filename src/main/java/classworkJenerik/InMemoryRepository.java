package classworkJenerik;

import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
public class InMemoryRepository implements Repository<BigInteger, LedgerEntry> {

    private final Map<BigInteger,LedgerEntry> storage = new HashMap<>();

    @Override
    public boolean save(LedgerEntry item) {
        if (item == null || item.getId() == null) {
            throw new NullPointerException("ID cannot be null");
        }

        LedgerEntry isSave = storage.putIfAbsent(item.getId(),item);
        return isSave == null;
    }

    @Override
    public Optional<LedgerEntry> findById(BigInteger id) {

        if (id == null) {
            throw new NullPointerException("ID cannot be null");
        }

        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public int saveAll(Collection<? extends LedgerEntry> items) {
        if (items == null) {
            throw new NullPointerException("collection cannot be null");
        }

        AtomicInteger count = new AtomicInteger();

         items.forEach(item-> {
           if(save(item)) {
               count.getAndIncrement();
           }
        });

        return count.get();
    }

    @Override
    public void copyTo(Collection<? super LedgerEntry> target) {
       target.addAll(storage.values());
    }

    @Override
    public List<LedgerEntry> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public int size() {
        return storage.size();
    }
}
