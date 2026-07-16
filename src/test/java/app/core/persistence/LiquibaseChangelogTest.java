package app.core.persistence;

import static org.assertj.core.api.Assertions.assertThatCode;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.OfflineConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;

class LiquibaseChangelogTest {

    @Test
    void validatesTheCompletePostgresqlChangelog() {
        assertThatCode(() -> {
            try (ClassLoaderResourceAccessor resources = new ClassLoaderResourceAccessor()) {
                OfflineConnection connection = new OfflineConnection(
                        "offline:postgresql?changeLogFile=target/liquibase-test-databasechangelog.csv", resources);
                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
                try (Liquibase liquibase = new Liquibase("liquibase/master.xml", resources, database)) {
                    liquibase.validate();
                }
            }
        }).doesNotThrowAnyException();
    }
}
