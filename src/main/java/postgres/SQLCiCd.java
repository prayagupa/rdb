package postgres;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLCiCd {

    final static String lookupFolder = "db/pg";

    public void applySQL() throws ClassNotFoundException {
        DatabaseConnection db = DatabaseConnection.postgres(
                "localhost",
                5432,
                "postgres",
                "postgres",
                "pachhigares"
        );
        File lookupFolderDir = new File(lookupFolder);

        db.getConnection().map(connection -> {
            try {
                Statement statement = connection.createStatement();
                File[] files = lookupFolderDir.listFiles((dir, name) -> name.endsWith(".sql"));

                char[] c = new char[1000];

                for (var f : files) {
                    var bufferedInputStream = new BufferedInputStream(new FileInputStream(f));
                    var destinationStream = new ByteArrayOutputStream();

                    int readMarker = bufferedInputStream.read();

                    while (readMarker != -1) {
                        destinationStream.write((byte) readMarker);
                        readMarker = bufferedInputStream.read();
                    }
                    var sqlString = destinationStream.toString();
                    boolean executed = statement.execute(sqlString);
                    if (executed) {
                        System.out.println("[INFO] SQL executed: " + sqlString);
                    } else {
                        System.out.println("[ERROR] SQL failed: " + sqlString);
                    }

                }

                statement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return "";
        });

    }

    public static void main(String[] args) throws ClassNotFoundException {
        new SQLCiCd().applySQL();
    }
}
