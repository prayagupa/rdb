package postgres;

import util.Util;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PostgresBigData100KWritePerf {

    private static final IntStream HUNDRED_K_USERS = IntStream.rangeClosed(1, 100000);
    private static final List<Integer> HUNDRED_K_USERS_ = IntStream.rangeClosed(1, 100000)
            .mapToObj($ -> $)
            .collect(Collectors.toList());

    private static DatabaseConnection databaseConnection;
    private static String USER_INSERTION = "INSERT INTO visiting_user(user_name) VALUES(?)";

    public PostgresBigData100KWritePerf() throws ClassNotFoundException {
        databaseConnection = DatabaseConnection.redshift(
                "localhost",
                5439,
                "database???",
                "username???",
                "password???"
        );
    }

    /**
     * ==========================================================================
     * ==========================================================================
     */
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        parallelUsers().join();
    }

    /**
     * ==========================================================================
     * ==========================================================================
     */
    private static void synchronousUsers() throws ClassNotFoundException, IOException {
        final PostgresBigData100KWritePerf postgresBigData100KWritePerf = new PostgresBigData100KWritePerf();
        var timeTaken = new HashMap<Integer, Long>();
        long start = System.currentTimeMillis();

        HUNDRED_K_USERS.forEach($ -> postgresBigData100KWritePerf.createUser($));

        long end = System.currentTimeMillis() - start;
        timeTaken.put(0, end);
        Util.writeToFile(
                PostgresBigData100KWritePerf.class.getName(),
                "write",
                "db/redshift/perf/100K_users_write_sync.csv",
                timeTaken);

        System.out.println("time taken for 100K users creation: " + end + "ms");
    }

    private final ForkJoinPool POOL = ForkJoinPool.commonPool();

    private static CompletableFuture<Integer> parallelUsers() throws ClassNotFoundException, IOException {
        final var bigDataPerf = new PostgresBigData100KWritePerf();
        List<CompletableFuture<Integer>> ps = new ArrayList<>();
        var timeTaken = new HashMap<Integer, Long>();

        long start = System.currentTimeMillis();

        HUNDRED_K_USERS_.forEach(id -> {
            ps.add(bigDataPerf.createUserAsync(id));
        });

        CompletableFuture[] cfs = ps.toArray(new CompletableFuture[ps.size()]);
        return CompletableFuture.allOf(cfs).thenApply($ -> {
            ps.stream().map($__ -> $__.join());
            long end = System.currentTimeMillis() - start;
            timeTaken.put(0, end);
            try {
                Util.writeToFile(PostgresBigData100KWritePerf.class.getName(),
                        "write",
                        "db/redshift/perf/100K_users_write_parallel.csv", timeTaken
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("time taken for 100K users creation: " + end + "ms");
            return 0;
        });
    }


    public static CompletableFuture<Integer> createUserAsync(int id) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[THREAD " + id + ":]" + Thread.currentThread().getName());
            return createUser(id);
        });
    }

    public static int createUser(int id) {
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

        return id;
    }
}
