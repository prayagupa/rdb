package com.testing;

import org.h2.tools.RunScript;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.LinkedList;

public class H2DbManager {
    private String db;
    private String url;

    private String user;
    private String password;

    public H2DbManager(String db, String url, String user, String password) {
        this.db = db;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public void applySqlScripts(LinkedList<String> files) {
        try (var conn = DriverManager.getConnection(url, user, password)) {
            for (var file : files) {
                FileInputStream is = new FileInputStream(new File(file));
                InputStreamReader reader = new InputStreamReader(is);
                System.out.println("---------------------------------------");
                System.out.println("executing: " + readInputStreamAsString(new FileInputStream(new File(file))));
                ResultSet execute = RunScript.execute(conn, reader);
                if (execute != null) {
                    while (execute.next()) {
                        System.out.println("executed: " + reader);
                    }
                }
                System.out.println("---------------------------------------");
            }
            ;
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public void applySqlScript(String file) {
        try (var conn = DriverManager.getConnection(url, user, password)) {
            FileInputStream is = new FileInputStream(new File(file));
            InputStreamReader reader = new InputStreamReader(is);

            System.out.println("---------------------------------------");
            System.out.println("executing: " + readInputStreamAsString(new FileInputStream(new File(file))));
            ResultSet execute = RunScript.execute(conn, reader);
            if (execute != null) {
                while (execute.next()) {
                    System.out.println("executed: " + reader);
                }
            }
            System.out.println("---------------------------------------");
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public void applySql(String customerSchema) {
        try (var conn = DriverManager.getConnection(url, user, password);
             var stmt = conn.createStatement()) {
            int i = stmt.executeUpdate(customerSchema);
            System.out.println("---------------------------------------");
            if (i > 0) {
                System.out.println("SUCCESS: " + i + ": " + customerSchema);
            } else {
                System.out.println("SUCCESS: " + customerSchema);
            }
            System.out.println("---------------------------------------");
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static String readInputStreamAsString(InputStream in)
            throws IOException {

        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }
}
