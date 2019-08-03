package postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.Optional;

//FIXME add pool
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

        this.dbUrl = url1;

        //Class.forName("com.amazon.redshift.jdbc42.Driver");
        Class.forName(driver);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url1);
        config.setUsername(this.username);
        config.setPassword(this.password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        connectionPool = new HikariDataSource(config);
    }

    public Optional<Connection> getRawConnection() {
        var start = System.currentTimeMillis();
        try {
            return Optional.ofNullable(DriverManager.getConnection(dbUrl, username, password));
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            System.out.println("Getting a connection for " + dbUrl + " took " + (System.currentTimeMillis() - start) + "ms");
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
            System.out.println("Getting a connection for " + dbUrl + " took " + (System.currentTimeMillis() - start) + "ms");
        }
    }
}
