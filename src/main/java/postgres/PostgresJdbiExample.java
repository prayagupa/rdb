package postgres;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;

import java.time.ZonedDateTime;

public class PostgresJdbiExample {

    static Jdbi jdbi = Jdbi.create("jdbc:postgresql://localhost:5432/postgres", "postgres", "root")
            .installPlugin(new PostgresPlugin());

    public static void main(String[] args) {
        MuseumVisit v = MuseumVisit.builder()
                .userId(1L)
                .musuemName("museum name")
                .visitStart(ZonedDateTime.now())
                .visitStartLocal(ZonedDateTime.now())
                .visitEnd(ZonedDateTime.now())
                .visitEndLocal(ZonedDateTime.now())
                .department("dept")
                .history("{\"name\" : \"test\"}")
                .build();

        jdbi.withHandle(h -> {
            int r = h.execute(
                    "INSERT INTO museum_visit(" +
                            "user_id, " +
                            "museum_name, " +
                            "department, " +
                            "visit_start_tz, " +
                            "visit_start_local, " +
                            "visit_end_tz, " +
                            "visit_end_local, " +
                            "visit_history) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?::json)",
                    v.getUserId(),
                    v.getMusuemName(),
                    v.getDepartment(),
                    v.getVisitStart(),
                    v.getVisitStartLocal(),
                    v.getVisitEnd(),
                    v.getVisitEndLocal(),
                    v.getHistory()
            );

            System.out.println(r);
            return r;
        });
    }
}
