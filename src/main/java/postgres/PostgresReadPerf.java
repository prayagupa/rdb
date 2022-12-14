package postgres;

import util.Util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * schema: db/pg/001-visit.sql
 * streaming: https://www.postgresql-archive.org/Streaming-ResultSet-td2168704.html
 */
public class PostgresReadPerf {

    private static final String READ_ONE_USER = "SELECT * FROM visit LIMIT 1";

    private static final String READ_ONE_VISIT = "SELECT * FROM museum_visit LIMIT 1";
    private static final String READ_ALL = "SELECT * FROM museum_visit " +
            "WHERE visit_start > ? AND visit_start < ?";

    public static Map<Integer, Long> timeMap = new HashMap<>();

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        DatabaseConnection databaseConnection = DatabaseConnection.redshift(
                "localhost",
                "database???",
                "admin",
                "password???"
        );

        int i = 0;
        while (i < 100) {
            readOne(i, databaseConnection);
            i++;
        }

        Util.writeToFile(PostgresReadPerf.class.getName(), "read", "db/redshift/perf/read_one_record.csv", timeMap);
    }

    private static void readOne(int i, DatabaseConnection databaseConnectionPool) {
        final var start = System.currentTimeMillis();

        Optional<Connection> connectionMaybe = databaseConnectionPool.getConnection();

        connectionMaybe.ifPresent(connection -> {
//            System.out.println("making a transaction");
            try {
                var statement = connection.prepareStatement(READ_ONE_USER);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    System.out.println(resultSet.getString("visit_id"));
                }

                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                long timeTaken = System.currentTimeMillis() - start;
//                System.out.println("time taken: " + timeTaken);
                timeMap.put(i, timeTaken);
                // time taken: 4186 to fetch one redshift record initially, later ~400ms
            }
        });
    }
}
