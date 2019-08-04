package postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class PostgresReadPerf {

    private static final String READ_ONE_USER = "SELECT * FROM visiting_user LIMIT 1";

    private static final String READ_ONE_VISIT = "SELECT * FROM museum_visit LIMIT 1";
    private static final String READ_ALL = "SELECT * FROM museum_visit " +
            "WHERE visit_start > ? AND visit_start < ?";

    public static void main(String[] args) throws ClassNotFoundException {
        DatabaseConnection databaseConnection = DatabaseConnection.redshift(
                "???.us-east-1.redshift.amazonaws.com",
                "analytics",
                "admin",
                "???"
        );

        readOne(databaseConnection);
    }

    private static void readOne(DatabaseConnection databaseConnection) {
        final var start = System.currentTimeMillis();

        Optional<Connection> connectionMaybe = databaseConnection.getConnection();

        connectionMaybe.ifPresent(connection -> {
            System.out.println("making a transaction");
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(READ_ONE_USER);
                while (resultSet.next()) {
                    System.out.println(resultSet.getString("user_id"));
                }

                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                System.out.println("time taken: " + (System.currentTimeMillis() - start));
                // time taken: 4186 to fetch one redshift record initially, later ~400ms
            }
        });
    }
}
