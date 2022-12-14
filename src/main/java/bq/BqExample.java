package bq;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class BqExample {
    private static final String CREDS = "src/main/resources/bq-service-account-credentials.json";
    private static final String projectId = "project???";
    BigQuery bigquery;

    public BqExample() throws IOException {
        File credentialsPath = new File(CREDS);

        GoogleCredentials credentials;
        try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
            credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
        }

        bigquery =
                BigQueryOptions.newBuilder()
                        .setCredentials(credentials)
                        .setProjectId(projectId)
                        .build()
                        .getService();

        System.out.println(credentials);
    }

    public String getDatasets() throws IOException {
        // Use the client.
        System.out.println("--------------------");
        System.out.println("Datasets:");
        var datasets = bigquery.listDatasets();
        System.out.println("--------------------");
        for (Dataset dataset : datasets.iterateAll()) {
            System.out.printf("Dataset: %s%n", dataset.getDatasetId().getDataset());
        }
        return "";
    }

    public void query() throws InterruptedException {
        QueryJobConfiguration queryConfig =
                QueryJobConfiguration.newBuilder(
                        "select c.* from `???.XXX.yyy` c LIMIT 10")
                        // Use standard SQL syntax for queries.
                        // See: https://cloud.google.com/bigquery/sql-reference/
                        .setUseLegacySql(false)
                        .build();

        // Create a job ID so that we can safely retry.
        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

        // Wait for the query to complete.
        queryJob = queryJob.waitFor();

        // Check for errors
        if (queryJob == null) {
            throw new RuntimeException("Job no longer exists");
        } else if (queryJob.getStatus().getError() != null) {
            // You can also look at queryJob.getStatus().getExecutionErrors() for all
            // errors, not just the latest one.
            throw new RuntimeException(queryJob.getStatus().getError().toString());
        }

        while(queryJob.getQueryResults().hasNextPage()) {
            System.out.println(queryJob.getQueryResults().toString());
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BqExample bqExample = new BqExample();
        bqExample.query();
    }
}
