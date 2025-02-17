package sports.center.com.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sports.center.com.service.AuthService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final EntityManagerFactory entityManagerFactory;

    private EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public boolean authenticateTrainee(String username, String password) {
        log.info("Authenticating trainee: {}", username);
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(t) FROM Trainee t WHERE t.username = :username AND t.password = :password",
                            Long.class
                    )
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean authenticateTrainer(String username, String password) {
        log.info("Authenticating trainer: {}", username);
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(t) FROM Trainer t WHERE t.username = :username AND t.password = :password",
                            Long.class
                    )
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult() > 0;
        } finally {
            em.close();
        }
    }
}