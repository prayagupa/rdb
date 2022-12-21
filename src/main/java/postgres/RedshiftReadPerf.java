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
public class RedshiftReadPerf {

    private static final String READ_ONE_USER = "select * from visit limit 1;";

    public static Map<Integer, Long> timeMap = new HashMap<>();

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        DatabaseConnection databaseConnection = DatabaseConnection.postgres(
                "localhost",
                5439,
                "database???",
                "username???",
                "password???"
        );

        int i = 0;
        while (i < 10) {
            readOne(i, databaseConnection);
            i++;
        }

        Util.writeToFile(RedshiftReadPerf.class.getName(), "read", "db/redshift/perf/read_one_record__.csv", timeMap);
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
                    System.out.println("SQL result: " + resultSet.getString("visit_id"));
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
