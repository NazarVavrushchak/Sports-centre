package sports.center.com.util.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sports.center.com.util.UsernameUtil;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class UsernameUtilTest {
    private final EntityManager entityManager = mock(EntityManager.class);
    private final UsernameUtil usernameUtil = new UsernameUtil(entityManager);

    @Test
    void generateUsername_WithNullInputs_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> usernameUtil.generateUsername(null, "Smith"));
    }

    @Test
    void generateUsername() {
        TypedQuery<String> query = Mockito.mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.getResultStream()).thenReturn(new HashSet<String>().stream());

        String username = usernameUtil.generateUsername("John", "Doe");

        assertEquals("john.doe", username);
        verify(entityManager).createQuery(anyString(), eq(String.class));
    }

    @Test
    void generateUsername_ExistingUsernames_ShouldIncrementIndex() {
        TypedQuery<String> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.getResultStream()).thenReturn(Set.of("john.doe", "john.doe1", "john.doe2").stream());

        String username = usernameUtil.generateUsername("John", "Doe");

        assertEquals("john.doe3", username);
        verify(entityManager).createQuery(anyString(), eq(String.class));
    }

    @Test
    void generateUsername_MaxIndexBorder_ShouldHandleProperly() {
        TypedQuery<String> query = mock(TypedQuery.class);
        Set<String> usernames = new HashSet<>();
        for (int i = 0; i <= 999; i++) {
            usernames.add("john.doe" + i);
        }
        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.getResultStream()).thenReturn(usernames.stream());

        String username = usernameUtil.generateUsername("John", "Doe");

        assertEquals("john.doe1000", username);
        verify(entityManager).createQuery(anyString(), eq(String.class));
    }

    @Test
    void generateUsername_EmptyStrings_ShouldThrowException() {
        TypedQuery<String> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.getResultStream()).thenReturn(new HashSet<String>().stream());

        assertThrows(IllegalArgumentException.class, () -> usernameUtil.generateUsername("", ""));
    }

    @Test
    void generateUsername_TrimmableInput_ShouldCreateValidUsername() {
        TypedQuery<String> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.getResultStream()).thenReturn(new HashSet<String>().stream());

        String username = usernameUtil.generateUsername("   John   ", "   Doe   ");

        assertEquals("john.doe", username);
        verify(entityManager).createQuery(anyString(), eq(String.class));
    }
}