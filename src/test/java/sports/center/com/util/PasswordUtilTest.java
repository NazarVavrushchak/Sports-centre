package sports.center.com.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordUtilTest {
    private static final int PASSWORD_LENGTH = 10;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Test
    void generatePassword_ShouldBeCorrectLength() {
        String password = PasswordUtil.generatePassword();
        assertEquals(PASSWORD_LENGTH, password.length(), "Generated password should have correct length.");
    }

    @Test
    void validCharacters() {
        String password = PasswordUtil.generatePassword();
        for (char ch : password.toCharArray()) {
            assertTrue(CHARACTERS.indexOf(ch) != -1, "All characters should be valid.");
        }
    }

    @Test
    void generateMultiplePasswords_ShouldBeUnique() {
        Set<String> generatedPasswords = new HashSet<>();
        final int sampleSize = 1000;
        for (int i = 0; i < sampleSize; i++) {
            generatedPasswords.add(PasswordUtil.generatePassword());
        }
        assertEquals(sampleSize, generatedPasswords.size(), "Generated passwords should be unique.");
    }
}