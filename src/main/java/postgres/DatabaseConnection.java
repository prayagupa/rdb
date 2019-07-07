package postgres;

import java.sql.*;
import java.util.Optional;

public class DatabaseConnection {

    private String username;
    private String password;
    private String dbUrl;

    public DatabaseConnection(String host,
                              int port,
                              String dbName,
                              String username,
                              String password) throws ClassNotFoundException {
        this.username = username;
        this.password = password;

        String url1 = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;

        this.dbUrl = url1;

        //Class.forName("com.amazon.redshift.jdbc42.Driver");
        Class.forName("org.postgresql.Driver");
    }

    public Optional<Connection> getConnection() {
        System.out.println("Getting a connection for " + dbUrl);
        try {
            return Optional.ofNullable(DriverManager.getConnection(dbUrl, username, password));
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
