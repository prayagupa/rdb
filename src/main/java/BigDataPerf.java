import postgres.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.IntStream;

public class BigDataPerf {

    private static DatabaseConnection databaseConnection;
    private static String USER_INSERTION = "INSERT INTO visiting_user(user_name) VALUES(?)";

    public BigDataPerf() throws ClassNotFoundException {
        databaseConnection = new DatabaseConnection(
                "???.%%%.us-east-1.rds.amazonaws.com",
                5432,
                "postgresql",
                "org.postgresql.Driver",
                "museum_visit",
                "postgres",
                "admin54321"
        );
    }

    public static void createUser(int id) {
        databaseConnection.getConnection().ifPresent($ -> {
            try {
                PreparedStatement statement = $.prepareStatement(USER_INSERTION);
                statement.setString(1, "user_" + id);
                statement.execute();
                statement.close();
                $.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) throws ClassNotFoundException {
        synchronousUsers();
    }

    private static void synchronousUsers() throws ClassNotFoundException {
        final BigDataPerf bigDataPerf = new BigDataPerf();
        long start = System.currentTimeMillis();

        IntStream.range(1, 100000).forEach($ -> bigDataPerf.createUser($));

        long end = System.currentTimeMillis() - start;

        System.out.println("time taken for 100K users creation: " + end + "ms");
    }

    private static void parallelUsers() throws ClassNotFoundException {
        final BigDataPerf bigDataPerf = new BigDataPerf();
        long start = System.currentTimeMillis();

        IntStream.range(1, 100000).parallel().forEach($ -> bigDataPerf.createUser($));

        long end = System.currentTimeMillis() - start;

        System.out.println("time taken for 100K users creation: " + end + "ms");
    }
}
