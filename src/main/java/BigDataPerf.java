import postgres.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BigDataPerf {

    private static final IntStream HUNDRED_K_USERS = IntStream.rangeClosed(1, 100000);
    private static final List<Integer> HUNDRED_K_USERS_ = IntStream.rangeClosed(1, 100000)
            .mapToObj($ -> $)
            .collect(Collectors.toList());

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

    public static void main(String[] args) throws ClassNotFoundException {
        parallelUsers().join();
    }

    private static void synchronousUsers() throws ClassNotFoundException {
        final BigDataPerf bigDataPerf = new BigDataPerf();
        long start = System.currentTimeMillis();

        HUNDRED_K_USERS.forEach($ -> bigDataPerf.createUser($));

        long end = System.currentTimeMillis() - start;

        System.out.println("time taken for 100K users creation: " + end + "ms");
    }

    private final ForkJoinPool POOL = ForkJoinPool.commonPool();

    private static CompletableFuture<Integer> parallelUsers() throws ClassNotFoundException {
        final var bigDataPerf = new BigDataPerf();
        long start = System.currentTimeMillis();
        List<CompletableFuture<Integer>> ps = new ArrayList<>();

        HUNDRED_K_USERS_.forEach($ -> {
            ps.add(bigDataPerf.createUserAsync($));
        });

        CompletableFuture[] cfs = ps.toArray(new CompletableFuture[ps.size()]);
        return CompletableFuture.allOf(cfs).thenApply($ -> {
            ps.stream().map($__ -> $__.join());
            long end = System.currentTimeMillis() - start;
            System.out.println("time taken for 100K users creation: " + end + "ms");
            return 0;
        });
    }
}
