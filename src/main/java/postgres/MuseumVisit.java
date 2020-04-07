package postgres;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Builder
@Getter
public class MuseumVisit {
    private long userId;
    private long visitId;
    private String musuemName;
    private String department;
    private ZonedDateTime visitStart;
    private ZonedDateTime visitStartLocal;
    private ZonedDateTime visitEnd;
    private ZonedDateTime visitEndLocal;
    private String history;
}
