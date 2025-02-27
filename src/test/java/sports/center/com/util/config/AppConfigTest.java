package sports.center.com.util.config;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.JpaTransactionManager;
import sports.center.com.config.AppConfig;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppConfigTest {

    private AppConfig appConfig;
    private EntityManagerFactory entityManagerFactoryMock;

    @BeforeEach
    void setUp() {
        appConfig = new AppConfig();
        entityManagerFactoryMock = mock(EntityManagerFactory.class);
    }

    @Test
    void testDataSource_NotNull() {
        DataSource dataSource = appConfig.dataSource();
        assertNotNull(dataSource, "DataSource should not be null");
    }

    @Test
    void testEntityManagerFactory_NotNull() {
        assertNotNull(appConfig.entityManagerFactory(), "EntityManagerFactory should not be null");
    }

    @Test
    void testTransactionManager_NotNull() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactoryMock);

        assertNotNull(transactionManager, "TransactionManager should not be null");
        assertNotNull(transactionManager.getEntityManagerFactory(), "EntityManagerFactory should not be null");
    }

    @Test
    void testValidator_NotNull() {
        assertNotNull(appConfig.validator(), "Validator should not be null");
    }

    @Test
    void testDataSource_NotDestroyedAfterRecreation() {
        DataSource firstInstance = appConfig.dataSource();
        DataSource secondInstance = appConfig.dataSource();

        assertNotNull(firstInstance, "First instance of DataSource should not be null");
        assertNotNull(secondInstance, "Second instance of DataSource should not be null");
        assertNotSame(firstInstance, secondInstance, "Each call should return a new instance");
    }

    @Test
    void testHibernateProperties_NotNull() throws Exception {
        Method method = AppConfig.class.getDeclaredMethod("hibernateProperties");
        method.setAccessible(true);

        Properties properties = (Properties) method.invoke(appConfig);

        assertNotNull(properties, "Hibernate properties should not be null");
        assertEquals("update", properties.getProperty("hibernate.hbm2ddl.auto"), "Incorrect Hibernate hbm2ddl.auto setting");
        assertEquals("org.hibernate.dialect.PostgreSQLDialect", properties.getProperty("hibernate.dialect"), "Incorrect Hibernate dialect");
    }
}

