package com.testing;

import org.h2.tools.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * $ ls -l /Users/a1353612/pya.*
 * -rw-r--r--  1 py  NA\Domain Users  20480 Jun  9 13:37 /Users/a1353612/pya.mv.db
 * -rw-r--r--  1 py  NA\Domain Users    261 Jun  9 13:37 /Users/a1353612/pya.trace.db
 */
public class H2Tests {

    Server h2MemServer;

    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static final String DATABASE_NAME = "pya";
    private static final String DB_URL = "jdbc:h2:~/" + DATABASE_NAME;

    private static final String USER = "sa";
    private static final String PASS = "";

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
//        h2MemServer = Server.createPgServer("-tcpPort", "9123", "-tcpAllowOthers").start();
        Class.forName(JDBC_DRIVER);
        String customerSchema = "CREATE TABLE customer " +
                "(id INTEGER not NULL, " +
                " first VARCHAR(255), " +
                " last VARCHAR(255), " +
                " age INTEGER, " +
                " PRIMARY KEY ( id ))";
        applySql(customerSchema);
    }

    @Test
    public void myTest() throws SQLException {
        try {
            var conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Creating table in database " + DATABASE_NAME);
            var stmt = conn.createStatement();

            String userDataSql = "INSERT INTO customer " + "VALUES (100, 'Ya', 'PY', 30)";
            stmt.executeUpdate(userDataSql);
            System.out.println(userDataSql);

            //
            String sql = "SELECT id, first, last, age FROM customer";
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()) {
                int id  = rs.getInt("id");
                int age = rs.getInt("age");
                String first = rs.getString("first");
                String last = rs.getString("last");

                // Display values
                System.out.print("ID: " + id);
                System.out.print(", Age: " + age);
                System.out.print(", First: " + first);
                System.out.println(", Last: " + last);
            }

            stmt.close();
            conn.close();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    @After
    public void teardown() {
//        h2MemServer.shutdown();
        String customerSchema = "DROP TABLE customer";
        applySql(customerSchema);
    }

    private void applySql(String customerSchema) {
        try (var conn = DriverManager.getConnection(DB_URL, USER, PASS);
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(customerSchema);
            System.out.println(customerSchema);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

}
