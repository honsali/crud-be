package app.core.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.cache")
public class CacheProperties {

    public static final int DEFAULT_MAX_ENTRIES = 100;
    public static final int DEFAULT_TTL_SECONDS = 3600;

    private long maxEntries = DEFAULT_MAX_ENTRIES;

    private int timeToLiveSeconds = DEFAULT_TTL_SECONDS;

    public long getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(long maxEntries) {
        this.maxEntries = maxEntries;
    }

    public int getTimeToLiveSeconds() {
        return timeToLiveSeconds;
    }

    public void setTimeToLiveSeconds(int timeToLiveSeconds) {
        this.timeToLiveSeconds = timeToLiveSeconds;
    }
}
