package postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public class DatabaseConnection {

    private String username;
    private String password;
    private String dbUrl;
    private HikariDataSource connectionPool;

    public DatabaseConnection(String host,
                              int port,
                              String dbIdentifier,
                              String driver,
                              String dbName,
                              String username,
                              String password) throws ClassNotFoundException {
        this.username = username;
        this.password = password;

        String url1 = "jdbc:"+ dbIdentifier + "://" + host + ":" + port + "/" + dbName;

        System.out.println("============= Creating database pool ============");
        this.dbUrl = url1;

        Class.forName(driver);

        System.out.println(dbUrl);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url1);
        config.setUsername(this.username);
        config.setPassword(this.password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setConnectionTimeout(1000);
        config.setIdleTimeout(10000);
        config.setMaximumPoolSize(2);
        config.setMaximumPoolSize(20);

        System.out.println("============= Created database config ============");
        connectionPool = new HikariDataSource(config);
        System.out.println("============= Created database pool ============");
    }

    public static DatabaseConnection redshift(String host,
                                              int port,
                                       String dbName,
                                       String username,
                                       String password) throws ClassNotFoundException {
        return new DatabaseConnection(
                host,
                port,
                "redshift",
                "com.amazon.redshift.jdbc42.Driver",
                dbName,
                username,
                password
        );
    }

    public static DatabaseConnection postgres(String host,
                                              int port,
                                              String dbName,
                                              String username,
                                              String password) throws ClassNotFoundException {
        return new DatabaseConnection(
                host,
                port,
                "postgresql",
                "org.postgresql.Driver",
                dbName,
                username,
                password
        );
    }

    public Optional<Connection> getRawConnection() {
        var start = System.currentTimeMillis();
        try {
            return Optional.ofNullable(DriverManager.getConnection(dbUrl, username, password));
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            System.out.println("Getting a connection for " + dbUrl + " took " +
                    (System.currentTimeMillis() - start) + "ms");
        }
    }

    public Optional<Connection> getConnection() {
        var start = System.currentTimeMillis();
        try {
            return Optional.ofNullable(connectionPool.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            System.out.println("Getting a connection for " + dbUrl + " took " +
                    (System.currentTimeMillis() - start) + "ms");
        }
    }
}
