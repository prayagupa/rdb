package postgres;

import java.io.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SQLCiCd {

    final static String lookupFolder = "db/pg";

    public void applySQL() throws ClassNotFoundException {
        DatabaseConnection db = DatabaseConnection.postgres(
                "localhost",
                "postgres",
                "postgres",
                "pachhigares"
        );
        File lookupFolderDir = new File(lookupFolder);

        db.getConnection().map(connection -> {
            try {
                Statement statement = connection.createStatement();
                File[] sqlFiles = lookupFolderDir.listFiles((dir, name) -> name.endsWith(".sql"));

                char[] c = new char[1000];

                for (var sqlFile : sqlFiles) {
                    var bufferedInputStream = new BufferedInputStream(new FileInputStream(sqlFile));
                    var destinationStream = new ByteArrayOutputStream();

                    int readMarker = bufferedInputStream.read();

                    while (readMarker != -1) {
                        destinationStream.write((byte) readMarker);
                        readMarker = bufferedInputStream.read();
                    }
                    var sqlString = destinationStream.toString();
                    boolean executed = statement.execute(sqlString);
                    if (executed) {
                        System.out.println("[INFO] SQL file " + sqlFile + " applied : " + sqlString);
                    } else {
                        System.out.println("[ERROR] SQL file " + sqlFile + " did not apply: " + sqlString);
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
