package sports.center.com.util.util;

import org.junit.jupiter.api.Test;
import sports.center.com.util.PasswordUtil;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordUtilTest {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;

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

    @Test
    void generatePassword_WithCustomLength() {
        int customLength = 20;
        String password = PasswordUtil.generatePassword(customLength);
        assertEquals(customLength, password.length(), "Generated password should match requested length.");
    }

    @Test
    void generatePassword_WithZeroLength_ShouldReturnEmptyString() {
        String password = PasswordUtil.generatePassword(0);
        assertEquals("", password, "Generated password with zero length should be empty.");
    }

    @Test
    void generatePassword_WithNegativeLength_ShouldReturnEmptyString() {
        String password = PasswordUtil.generatePassword(-5);
        assertEquals("", password, "Generated password with negative length should be empty.");
    }
}
