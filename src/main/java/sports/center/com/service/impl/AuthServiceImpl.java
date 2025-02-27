package sports.center.com.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sports.center.com.service.AuthService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

    @Override
    public boolean authenticateRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return false;
        }

        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String[] values = credentials.split(":", 2);

        if (values.length != 2) {
            return false;
        }

        String username = values[0];
        String password = values[1];

        return authenticateTrainee(username, password) || authenticateTrainer(username, password);
    }
}