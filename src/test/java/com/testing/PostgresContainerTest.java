package com.testing;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class PostgresContainerTest {

    private final Set<HikariDataSource> datasourcesForCleanup = new HashSet<>();

    @ClassRule
    public static PostgreSQLContainer postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("sa")
            .withPassword("sa")
            .withExposedPorts(5555);

    @Before
    public void before() {
    }

    @Test
    public void test() {
        Assert.assertEquals(1, 1);
    }

    @Test
    public void returnsTodaysDate() throws SQLException {

        DataSource ds = getDataSource(postgresContainer);
        var statement = ds.getConnection().createStatement();
        var rs = statement.executeQuery("SELECT now()");

        rs.next();

        var now = rs.getDate(1);
        Assert.assertEquals("NOW(): ", LocalDate.now().toString(), now.toString());
    }

    DataSource getDataSource(JdbcDatabaseContainer container) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(container.getJdbcUrl());
        hikariConfig.setUsername(container.getUsername());
        hikariConfig.setPassword(container.getPassword());
        hikariConfig.setDriverClassName(container.getDriverClassName());

        final HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        datasourcesForCleanup.add(dataSource);

        return dataSource;
    }

    @After
    public void teardown() {
        datasourcesForCleanup.forEach(HikariDataSource::close);
    }
}
