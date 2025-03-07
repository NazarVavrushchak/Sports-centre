package sports.center.com.util.healthIndicator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import sports.center.com.healthIndicator.DatabaseConnectionHealthIndicator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatabaseConnectionHealthIndicatorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private DatabaseConnectionHealthIndicator databaseHealthIndicator;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        databaseHealthIndicator = new DatabaseConnectionHealthIndicator(dataSource);
    }

    @Test
    void testHealth_DatabaseConnected() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(1)).thenReturn(true);

        Health health = databaseHealthIndicator.health();

        assertEquals("UP", health.getStatus().getCode());
        assertEquals("Connected", health.getDetails().get("database"));

        verify(dataSource).getConnection();
        verify(connection).isValid(1);
        verify(connection).close();
    }

    @Test
    void testHealth_DatabaseConnectionNotValid() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(1)).thenReturn(false);

        Health health = databaseHealthIndicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("Connection is not valid", health.getDetails().get("database"));

        verify(dataSource).getConnection();
        verify(connection).isValid(1);
        verify(connection).close();
    }

    @Test
    void testHealth_DatabaseConnectionFailed() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Database error"));

        Health health = databaseHealthIndicator.health();

        assertEquals("DOWN", health.getStatus().getCode());
        assertEquals("Connection failed", health.getDetails().get("database"));

        verify(dataSource).getConnection();
    }
}
