import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * 3 cols with size around ~125bytes
 * total time taken to insert the batch = 31 ms
 * total time taken = 0 s
 */
public class SingleBatchTransactionPerf {

    private static Connection getDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521/xe",
                "SYSTEM",
                "oracle"
        );
    }

    public static int[] writeInABatchWithCompiledQuery(int records) {
        PreparedStatement preparedStatement;

        try {
            Connection connection = getDatabaseConnection();
            connection.setAutoCommit(true);

            var compiledQuery = "INSERT INTO customer(id, name, address, loyalty_point, username)" +
                    " VALUES" + "(?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(compiledQuery);

            for(var index = 1; index <= records; index++) {
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
                preparedStatement.addBatch();
            }

            var start = System.currentTimeMillis();
            int[] inserted = preparedStatement.executeBatch();
            var end = System.currentTimeMillis();

            System.out.println("total time taken to insert the batch = " + (end - start) + " ms");
            System.out.println("total time taken = " + (end - start)/records + " s");

            preparedStatement.close();
            connection.close();

            return inserted;

        } catch (SQLException ex) {
            System.err.println("SQLException information");
            while (ex != null) {
                System.err.println("Error msg: " + ex.getMessage());
                ex = ex.getNextException();
            }
            throw new RuntimeException("Error");
        }
    }

    public static void main(String[] args) {
        writeInABatchWithCompiledQuery(1000);
    }
}
