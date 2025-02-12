package sports.center.com.util;

import java.util.concurrent.ThreadLocalRandom;

public class PasswordUtil {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;

    public static String generatePassword() {
        return generatePassword(PASSWORD_LENGTH);
    }

    public static String generatePassword(int length) {
        StringBuilder password = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }
}