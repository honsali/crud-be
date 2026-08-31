package app.core.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import app.core.security.config.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoginAttemptLimiterTest {

    private MutableClock clock;
    private SecurityProperties properties;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-08-10T12:00:00Z"));
        properties = new SecurityProperties();
        properties.getLoginRateLimit().setUsernameMaxAttempts(2);
        properties.getLoginRateLimit().setUsernameWindow(Duration.ofMinutes(5));
        properties.getLoginRateLimit().setAddressMaxAttempts(3);
        properties.getLoginRateLimit().setAddressWindow(Duration.ofMinutes(10));
        properties.getLoginRateLimit().setMaximumSize(10);
    }

    @Test
    void blocksIndependentlyAtTheUsernameAndAddressThresholds() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(properties, clock);
        String username = limiter.canonicalUsername(" Alice.Admin ");
        assertThat(username).isEqualTo("alice.admin");

        assertThat(limiter.tryAcquire(username, "10.0.0.1")).isTrue();
        assertThat(limiter.tryAcquire(username, "10.0.0.2")).isTrue();
        assertThat(limiter.tryAcquire(username, "10.0.0.3")).isFalse();

        assertThat(limiter.tryAcquire("first", "10.0.0.9")).isTrue();
        assertThat(limiter.tryAcquire("second", "10.0.0.9")).isTrue();
        assertThat(limiter.tryAcquire("third", "10.0.0.9")).isTrue();
        assertThat(limiter.tryAcquire("new-user", "10.0.0.9")).isFalse();
    }

    @Test
    void windowExpirationAndSuccessResetAreDeterministic() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(properties, clock);
        assertThat(limiter.tryAcquire("alice", "10.0.0.1")).isTrue();
        assertThat(limiter.tryAcquire("alice", "10.0.0.2")).isTrue();
        assertThat(limiter.tryAcquire("alice", "10.0.0.3")).isFalse();

        limiter.recordSuccess("alice");
        assertThat(limiter.tryAcquire("alice", "10.0.0.3")).isTrue();
        assertThat(limiter.tryAcquire("alice", "10.0.0.4")).isTrue();
        assertThat(limiter.tryAcquire("alice", "10.0.0.5")).isFalse();
        clock.advance(Duration.ofMinutes(5).plusSeconds(1));
        assertThat(limiter.tryAcquire("alice", "10.0.0.6")).isTrue();
    }

    @Test
    void successResetsOnlyTheProvenUsernameAndNeverTheAddress() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(properties, clock);
        assertThat(limiter.tryAcquire("alice", "10.0.0.9")).isTrue();
        limiter.recordSuccess("alice");
        assertThat(limiter.tryAcquire("alice", "10.0.0.9")).isTrue();
        limiter.recordSuccess("alice");
        assertThat(limiter.tryAcquire("alice", "10.0.0.9")).isTrue();
        limiter.recordSuccess("alice");

        assertThat(limiter.tryAcquire("bob", "10.0.0.9")).isFalse();
    }

    @Test
    void caffeineCachesRemainBounded() {
        properties.getLoginRateLimit().setMaximumSize(2);
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(properties, clock);
        for (int index = 0; index < 100; index++) {
            limiter.tryAcquire("user-" + index, "address-" + index);
        }
        assertThat(limiter.usernameCacheSize()).isLessThanOrEqualTo(2);
        assertThat(limiter.addressCacheSize()).isLessThanOrEqualTo(2);
    }

    @Test
    void concurrentAdmissionsNeverExceedTheConfiguredThreshold() throws Exception {
        properties.getLoginRateLimit().setUsernameMaxAttempts(3);
        properties.getLoginRateLimit().setAddressMaxAttempts(100);
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(properties, clock);
        int requestCount = 16;
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        List<Future<Boolean>> admissions = new ArrayList<>();
        try {
            for (int index = 0; index < requestCount; index++) {
                admissions.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Le départ concurrent n'a pas été libéré");
                    }
                    return limiter.tryAcquire("same.user", "10.0.0.42");
                }));
            }
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            List<Boolean> results = new ArrayList<>();
            for (Future<Boolean> admission : admissions) {
                results.add(admission.get(5, TimeUnit.SECONDS));
            }
            assertThat(results).filteredOn(Boolean.TRUE::equals).hasSize(3);
            assertThat(results).filteredOn(Boolean.FALSE::equals).hasSize(requestCount - 3);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
