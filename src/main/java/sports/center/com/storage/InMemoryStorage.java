package sports.center.com.storage;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryStorage {

    private final Map<String, Map<Long, Object>> storage = new HashMap<>();

    public <T> Map<Long, T> getNamespace(String namespace) {
        return (Map<Long, T>) storage.computeIfAbsent(namespace, k -> new HashMap<>());
    }

    public <T> void loadInitialData(String namespace, List<T> data) {
        Map<Long, T> namespaceMap = getNamespace(namespace);
        long id = 1;
        for (T item : data) {
            namespaceMap.put(id++, item);
        }
    }
}