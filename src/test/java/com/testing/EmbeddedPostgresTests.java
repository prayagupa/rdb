package com.testing;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Random;

/**
 * https://stackoverflow.com/a/9407940/432903
 *
 * ls -l /tmp/pg_unit_tests_/data/
 * total 104
 * -rw-------   1 ya_karnali  wheel      3 Jun 15 10:21 PG_VERSION
 * drwx------   5 ya_karnali  wheel    160 Jun 15 10:22 base
 * -rw-r--r--   1 ya_karnali  wheel      0 Jun 15 10:22 epg-lock
 * drwx------  61 ya_karnali  wheel   1952 Jun 15 10:22 global
 * drwx------   2 ya_karnali  wheel     64 Jun 15 10:21 pg_commit_ts
 * drwx------   2 ya_karnali  wheel     64 Jun 15 10:21 pg_dynshmem
 * -rw-------   1 ya_karnali  wheel   4513 Jun 15 10:21 pg_hba.conf
 * -rw-------   1 ya_karnali  wheel   1636 Jun 15 10:21 pg_ident.conf
 * drwx------   5 ya_karnali  wheel    160 Jun 15 10:22 pg_logical
 * drwx------   4 ya_karnali  wheel    128 Jun 15 10:21 pg_multixact
 * drwx------   3 ya_karnali  wheel     96 Jun 15 10:22 pg_notify
 * drwx------   2 ya_karnali  wheel     64 Jun 15 10:21 pg_replslot
 * drwx------   2 ya_karnali  wheel     64 Jun 15 10:21 pg_serial
 * drwx------   2 ya_karnali  wheel     64 Jun 15 10:21 pg_snapshots
 * drwx------   2 ya_karnali  wheel     64 Jun 15 10:21 pg_stat
 * drwx------   3 ya_karnali  wheel     96 Jun 15 10:22 pg_stat_tmp
 * drwx------   3 ya_karnali  wheel     96 Jun 15 10:21 pg_subtrans
 * drwx------   2 ya_karnali  wheel     64 Jun 15 10:21 pg_tblspc
 * drwx------   2 ya_karnali  wheel     64 Jun 15 10:21 pg_twophase
 * drwx------   4 ya_karnali  wheel    128 Jun 15 10:21 pg_wal
 * drwx------   3 ya_karnali  wheel     96 Jun 15 10:21 pg_xact
 * -rw-------   1 ya_karnali  wheel     88 Jun 15 10:21 postgresql.auto.conf
 * -rw-------   1 ya_karnali  wheel  22953 Jun 15 10:21 postgresql.conf
 * -rw-------   1 ya_karnali  wheel    289 Jun 15 10:22 postmaster.opts
 * -rw-------   1 ya_karnali  wheel     91 Jun 15 10:22 postmaster.pid
 */
public class EmbeddedPostgresTests {

    private static final int MIN_PORT = 6000;
    private static final int MAX_PORT = 7000;

    private int port;
    private EmbeddedPostgres pg;

    @Before
    public void beforeTests() {
        port = new Random().nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;

        try {
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            System.out.println("Starting db");
            pg = EmbeddedPostgres.builder()
                    .setServerConfig("fsync", "off")
                    .setServerConfig("full_page_writes", "off")
                    .setServerConfig("synchronous_commit", "off")
                    .setPort(port)
                    .setDataDirectory(
                            Files.createDirectories(
                                    Path.of("/tmp")
                                            .resolve("pg_unit_tests_")
                                            .resolve("data")
                            ))
                    .start();

            applySchema("db/pg/001-visit.sql");
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        } catch (IOException | SQLException e) {
//            throw new RuntimeException("Error starting postgres", e);
            e.printStackTrace();
        }
    }

    @Test
    public void testPostgresConnection() {
        String postgresUrl = String.format("jdbc:postgresql://localhost:%s/postgres?user=postgres&password=", port);
        System.out.println(postgresUrl);
        try (
                var conn = DriverManager.getConnection(postgresUrl);
//                var conn1 = pg.getPostgresDatabase().getConnection();
                var stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT NOW()");
            System.out.println("---------------------------------------");
            if (rs.next()) {
                System.out.println(rs.getString(1));
            }

            System.out.println("---------------------------------------");
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    @After
    public void afterTests() {
        try {
            System.out.println("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
            System.out.println("Closing db");
            pg.close();
            System.out.println("yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
        } catch (IOException e) {
//            throw new RuntimeException("Error closing postgres", e);
            e.printStackTrace();
        }
    }

    private void applySchema(String schema) throws SQLException, IOException {
        try (var conn = pg.getPostgresDatabase().getConnection();
             var st = conn.createStatement()) {
            var ex = st.executeUpdate(Util.readInputStreamAsString(new FileInputStream(schema)));
        }
    }
}
