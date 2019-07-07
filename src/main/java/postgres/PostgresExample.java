package postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class PostgresExample {

    public static void main(String[] args) throws ClassNotFoundException {
        DatabaseConnection databaseConnection = new DatabaseConnection(
                "???.???.us-east-1.redshift.amazonaws.com",
                5439,
                "???",
                "???",
                "???"
        );

        Optional<Connection> connectionMaybe = databaseConnection.getConnection();

        String sql = "SELECT * FROM Customer LIMIT 1";

        connectionMaybe.ifPresent(connection -> {
            System.out.println("making a transaction");
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    System.out.println(resultSet.getString("customer_id"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
