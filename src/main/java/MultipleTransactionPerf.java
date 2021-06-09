import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * CREATE TABLE customer (
 * id NUMBER(10) NOT NULL,
 * name VARCHAR(20),
 * address VARCHAR(40),
 * loyalty_point NUMBER(10),
 * username VARCHAR(50)
 * );
 * <p>
 * total time taken = 2251 ms
 * avg total time taken = 2 ms
 *
 * for 140 size
 * total time taken = 2181 ms
 * avg total time taken = 2 ms
 *
 * total time taken = 2306 ms
 * avg total time taken = 2 ms
 */
public class MultipleTransactionPerf {

    private static Connection getDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521/xe",
                "SYSTEM",
                "oracle"
        );
    }

    private static int writeWithCompileQuery(int records) {
        PreparedStatement preparedStatement;

        try {
            Connection connection = getDatabaseConnection();
            connection.setAutoCommit(true);

            String compiledQuery = "INSERT INTO CUSTOMER(id, name, address, loyalty_point, username)" +
                    " VALUES" + "(?, ?, ?, ?, ?)";

            preparedStatement = connection.prepareStatement(compiledQuery);

            long start = System.currentTimeMillis();

            for (int index = 1; index < records; index++) {
                preparedStatement.setInt(1, index);
                preparedStatement.setString(2,
                        "customer name -" +
                                UUID.randomUUID().toString() + ":" +
                                UUID.randomUUID().toString() + ":" +
                                UUID.randomUUID().toString()
                );
                preparedStatement.setString(3,
                        "address - " +
                                UUID.randomUUID().toString() + ":" +
                                UUID.randomUUID().toString() + ":" +
                                UUID.randomUUID().toString()
                );
                preparedStatement.setInt(4, index);
                preparedStatement.setString(5,
                        "username-" +
                                UUID.randomUUID().toString() + ":" +
                                UUID.randomUUID().toString() + ":" +
                                UUID.randomUUID().toString()
                );

                long startInternal = System.currentTimeMillis();
                long inserted = preparedStatement.executeUpdate();
                System.out.println("each transaction time taken = " + (System.currentTimeMillis() - startInternal) + " ms");
            }

            long end = System.currentTimeMillis();
            System.out.println("total time taken = " + (end - start) + " ms");
            System.out.println("avg total time taken = " + (end - start) / records + " ms");

            preparedStatement.close();
            connection.close();

            return records;
        } catch (SQLException ex) {
            System.err.println("SQLException information: ");
            while (ex != null) {
                System.err.println("Error msg: " + ex.getMessage());
                ex = ex.getNextException();
            }

            return 0;
        }
    }

    public static void main(String[] args) {
        writeWithCompileQuery(10000);
    }
}
