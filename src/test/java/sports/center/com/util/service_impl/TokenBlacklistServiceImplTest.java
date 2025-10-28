package sports.center.com.util.service_impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sports.center.com.service.impl.TokenBlacklistServiceImpl;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceImplTest {

    @InjectMocks
    private TokenBlacklistServiceImpl tokenBlacklistService;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field blacklistedTokensField = TokenBlacklistServiceImpl.class.getDeclaredField("blacklistedTokens");
        blacklistedTokensField.setAccessible(true);
        Set<String> blacklistedTokens = (Set<String>) blacklistedTokensField.get(tokenBlacklistService);
        blacklistedTokens.clear();
        blacklistedTokensField.setAccessible(false);
    }

    @Test
    void blacklistToken_Success() {
        String token = "validToken123";

        tokenBlacklistService.blacklistToken(token);

        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
    }

    @Test
    void blacklistToken_NullToken_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tokenBlacklistService.blacklistToken(null);
        });

        assertEquals("Token cannot be null or empty", exception.getMessage());
    }

    @Test
    void blacklistToken_EmptyToken_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tokenBlacklistService.blacklistToken("");
        });

        assertEquals("Token cannot be null or empty", exception.getMessage());
    }

    @Test
    void isTokenBlacklisted_TokenIsBlacklisted_ShouldReturnTrue() {
        String token = "blacklistedToken";
        tokenBlacklistService.blacklistToken(token);

        boolean result = tokenBlacklistService.isTokenBlacklisted(token);

        assertTrue(result);
    }

    @Test
    void isTokenBlacklisted_TokenNotBlacklisted_ShouldReturnFalse() {
        String token = "notBlacklistedToken";

        boolean result = tokenBlacklistService.isTokenBlacklisted(token);

        assertFalse(result);
    }

    @Test
    void isTokenBlacklisted_NullToken_ShouldReturnFalse() {
        boolean result = tokenBlacklistService.isTokenBlacklisted(null);

        assertFalse(result);
    }
}
