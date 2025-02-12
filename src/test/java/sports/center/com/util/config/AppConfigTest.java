package sports.center.com.util.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.jpa.JpaTransactionManager;
import sports.center.com.config.AppConfig;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {
    private static AnnotationConfigApplicationContext context;

    @BeforeAll
    static void setup() {
        System.setProperty("DATASOURCE_URL", "jdbc:postgresql://localhost:5432/Sports-Centre");
        System.setProperty("DATASOURCE_USER", "postgres");
        System.setProperty("DATASOURCE_PASSWORD", "postgres");

        context = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    @Test
    void testDataSource() {
        DataSource dataSource = context.getBean(DataSource.class);
        assertNotNull(dataSource, "DataSource couldn't be null");
    }

    @Test
    void testTransactionManager() {
        JpaTransactionManager transactionManager = context.getBean(JpaTransactionManager.class);
        assertNotNull(transactionManager, "TransactionManager couldn't be null");
        assertNotNull(transactionManager.getEntityManagerFactory(), "EntityManagerFactory couldn't be null");
    }

    @Test
    void testDatabaseConnection() {
        DataSource dataSource = context.getBean(DataSource.class);
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertFalse(connection.isClosed(), "Database connection shouldn't be closed");
            }
        }, "Error occurred while trying to connect to the database");
    }

    @Test
    void testDatabaseDriver() {
        DataSource dataSource = context.getBean(DataSource.class);
        assertDoesNotThrow(() -> {
            try (var connection = dataSource.getConnection()) {
                assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName(), "Database driver is incorrect");
            }
        }, "Error occurred while checking the database driver");
    }

    @Test
    void testEnvironmentVariables() {
        assertEquals("jdbc:postgresql://localhost:5432/Sports-Centre", System.getProperty("DATASOURCE_URL"), "Incorrect DATASOURCE_URL");
        assertEquals("postgres", System.getProperty("DATASOURCE_USER"), "Incorrect DATASOURCE_USER");
        assertEquals("postgres", System.getProperty("DATASOURCE_PASSWORD"), "Incorrect DATASOURCE_PASSWORD");
    }
}