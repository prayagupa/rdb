package postgres.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Spring configuration that wires up:
 * <ul>
 *   <li>A HikariCP {@link DataSource} pointing at the local museumdb instance.</li>
 *   <li>A JPA {@link EntityManagerFactory} backed by Hibernate 6.</li>
 *   <li>A {@link JpaTransactionManager} for declarative {@code @Transactional} support.</li>
 *   <li>Spring Data JPA repositories from the {@code postgres.repository} package.</li>
 * </ul>
 *
 * <p>Adjust connection properties to match your environment (see {@code docker-compose.yml}).
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "postgres.repository")
public class PersistenceConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:postgresql://localhost:5432/museumdb");
        cfg.setUsername("mu");
        cfg.setPassword("mu");
        cfg.setMaximumPoolSize(10);
        cfg.setConnectionTimeout(3_000);
        return new HikariDataSource(cfg);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("postgres.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        em.setJpaVendorAdapter(vendorAdapter);

        Properties jpaProps = new Properties();
        // Let the DDL scripts own the schema; Hibernate only validates / uses it.
        jpaProps.setProperty("hibernate.hbm2ddl.auto", "none");
        jpaProps.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProps.setProperty("hibernate.format_sql", "true");
        // Hibernate 6 maps java.util.UUID to PostgreSQL's native uuid type automatically.
        em.setJpaProperties(jpaProps);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
