package com.testing;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class PostgresTest {

    private final Set<HikariDataSource> datasourcesForCleanup = new HashSet<>();

    @Rule
    public GenericContainer postgresContainer = new GenericContainer<>("postgres:latest")
            .withExposedPorts(5432);

    @Test
    public void test() {
        Assert.assertEquals(1, 1);
    }

    @Test
    public void returnsSimpleData() throws SQLException {
        try (PostgreSQLContainer postgres = new PostgreSQLContainer<>()) {
            postgres.start();

            DataSource ds = getDataSource(postgres);
            var statement = ds.getConnection().createStatement();
            var rs = statement.executeQuery("SELECT 1");

            rs.next();

            int resultSetInt = rs.getInt(1);
            Assert.assertEquals("A basic SELECT query succeeds", 1, resultSetInt);
        }
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
