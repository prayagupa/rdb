FROM postgres:15-alpine AS pg

WORKDIR /sql

COPY ./db/pg /sql/

COPY --from=golang:1.16-alpine /usr/local/go/ /usr/local/go/

RUN mkdir /gopath
ENV GOPATH /gopath
ENV PATH "/usr/local/go/bin:${GOPATH}/bin:${PATH}"

## https://github.com/achiku/planter
RUN go get github.com/achiku/planter

RUN mkdir /erd \
 && su postgres -c "initdb && pg_ctl -D /var/lib/postgresql/data start" \
 && psql -U postgres -f /sql/001-visit.sql \
 && planter postgres://postgres@localhost?sslmode=disable -o /erd/schema.uml

FROM plantuml/plantuml:sha-cfe2b60 AS erd

WORKDIR /erd

COPY --from=pg /erd/ /erd/

RUN java -jar /opt/plantuml.jar -verbose schema.uml