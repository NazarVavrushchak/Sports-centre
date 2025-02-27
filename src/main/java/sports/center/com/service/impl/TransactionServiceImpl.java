package sports.center.com.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sports.center.com.service.TransactionService;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {
    public String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
}