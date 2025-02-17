package sports.center.com.service;

public interface AuthService {
    boolean authenticateTrainee(String username, String password);

    boolean authenticateTrainer(String username, String password);
}