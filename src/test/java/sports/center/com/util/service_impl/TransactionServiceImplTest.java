package sports.center.com.util.service_impl;

import org.junit.jupiter.api.Test;
import sports.center.com.service.TransactionService;
import sports.center.com.service.impl.TransactionServiceImpl;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceImplTest {

    private final TransactionService transactionService = new TransactionServiceImpl();

    @Test
    void generateTransactionId_ShouldReturnNonNullAndUniqueValue() {
        String transactionId1 = transactionService.generateTransactionId();
        String transactionId2 = transactionService.generateTransactionId();

        assertNotNull(transactionId1, "Transaction ID should not be null");
        assertNotNull(transactionId2, "Transaction ID should not be null");

        assertFalse(transactionId1.isEmpty(), "Transaction ID should not be empty");
        assertFalse(transactionId2.isEmpty(), "Transaction ID should not be empty");

        assertNotEquals(transactionId1, transactionId2, "Transaction IDs should be unique");

        Set<String> generatedIds = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            generatedIds.add(transactionService.generateTransactionId());
        }

        assertEquals(100, generatedIds.size(), "All generated IDs should be unique");
    }

    @Test
    void generateTransactionId_ShouldBeThreadSafe() throws InterruptedException {
        final int threadCount = 10;
        final int iterationsPerThread = 100;
        Set<String> generatedIds = new HashSet<>();
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterationsPerThread; j++) {
                    synchronized (generatedIds) {
                        generatedIds.add(transactionService.generateTransactionId());
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(threadCount * iterationsPerThread, generatedIds.size(), "All generated IDs should be unique across threads");
    }
}