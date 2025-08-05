package app.core.liquibase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.liquibase.DataSourceClosingSpringLiquibase;
import org.springframework.util.StopWatch;
import liquibase.exception.LiquibaseException;

public class AsyncSpringLiquibase extends DataSourceClosingSpringLiquibase {

    public static final String STARTING_ASYNC_MESSAGE = "Starting Liquibase asynchronously, your database might not be ready at startup!";
    public static final String STARTED_MESSAGE = "Liquibase has updated your database in {} ms";
    public static final String EXCEPTION_MESSAGE = "Liquibase could not start correctly, your database is NOT ready:  {}";
    public static final long SLOWNESS_THRESHOLD = 5; // seconds
    public static final String SLOWNESS_MESSAGE = "Warning, Liquibase took more than {} seconds to start up!";

    private final Logger logger = LoggerFactory.getLogger(AsyncSpringLiquibase.class);

    private final Executor executor;

    public AsyncSpringLiquibase(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void afterPropertiesSet() throws LiquibaseException {
        try (Connection connection = getDataSource().getConnection()) {
            executor.execute(() -> {
                try {
                    logger.warn(STARTING_ASYNC_MESSAGE);
                    initDb();
                } catch (LiquibaseException e) {
                    logger.error(EXCEPTION_MESSAGE, e.getMessage(), e);
                }
            });
        } catch (SQLException e) {
            logger.error(EXCEPTION_MESSAGE, e.getMessage(), e);
        }

    }

    protected void initDb() throws LiquibaseException {
        StopWatch watch = new StopWatch();
        watch.start();
        super.afterPropertiesSet();
        watch.stop();
        logger.debug(STARTED_MESSAGE, watch.getTotalTimeMillis());
        if (watch.getTotalTimeMillis() > SLOWNESS_THRESHOLD * 1000L) {
            logger.warn(SLOWNESS_MESSAGE, SLOWNESS_THRESHOLD);
        }
    }
}
