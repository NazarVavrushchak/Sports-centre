package sports.center.com.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InMemoryStorageTest {
    private InMemoryStorage inMemoryStorage;

    @BeforeEach
    void setUp() {
        inMemoryStorage = new InMemoryStorage();
    }

    @Test
    void testGetNamespace_WhenNamespaceNotExist_ShouldCreateNewMap() {
        String namespace = "testNamespace";

        Map<Long, Object> result = inMemoryStorage.getNamespace(namespace);

        assertNotNull(result);
        assertTrue(result instanceof Map);
    }

    @Test
    void testGetNamespace_WhenNamespaceExist_ShouldReturnExistingMap() {
        String namespace = "testNamespace";
        inMemoryStorage.getNamespace(namespace);

        Map<Long, Object> result = inMemoryStorage.getNamespace(namespace);

        assertNotNull(result);
    }

    @Test
    void testLoadInitialData_ShouldLoadDataCorrectly() {
        String namespace = "testNamespace";
        List<String> data = Arrays.asList("Item1", "Item2", "Item3");

        inMemoryStorage.loadInitialData(namespace, data);
        Map<Long, String> result = inMemoryStorage.getNamespace(namespace);

        assertEquals(3, result.size());
        assertEquals("Item1", result.get(1L));
        assertEquals("Item2", result.get(2L));
        assertEquals("Item3", result.get(3L));
    }
}