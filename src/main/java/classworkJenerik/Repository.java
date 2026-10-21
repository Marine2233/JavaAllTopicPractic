package classworkJenerik;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Repository <K,T extends Identifiable<K>>{
    boolean save(T item);
    Optional<T> findById(K id);
    int saveAll(Collection<? extends T> items);
    void copyTo(Collection<? super T> target);
    List<T> findAll();
    int size();

}
