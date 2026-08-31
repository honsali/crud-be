package app.core.security.ratelimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

import app.core.security.config.SecurityProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.stereotype.Component;

@Component
public class LoginAttemptLimiter {

    private final Cache<String, Attempts> usernames;
    private final Cache<String, Attempts> addresses;
    private final Clock clock;
    private final int usernameMaximum;
    private final Duration usernameWindow;
    private final int addressMaximum;
    private final Duration addressWindow;
    private final Object admissionMonitor = new Object();

    public LoginAttemptLimiter(SecurityProperties properties, Clock clock) {
        SecurityProperties.LoginRateLimit config = properties.getLoginRateLimit();
        validate(config);
        Duration retention = config.getUsernameWindow().compareTo(config.getAddressWindow()) >= 0
                ? config.getUsernameWindow() : config.getAddressWindow();
        this.usernames = newCache(config.getMaximumSize(), retention);
        this.addresses = newCache(config.getMaximumSize(), retention);
        this.clock = clock;
        this.usernameMaximum = config.getUsernameMaxAttempts();
        this.usernameWindow = config.getUsernameWindow();
        this.addressMaximum = config.getAddressMaxAttempts();
        this.addressWindow = config.getAddressWindow();
    }

    public String canonicalUsername(String username) {
        return username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
    }

    public boolean tryAcquire(String canonicalUsername, String sourceAddress) {
        synchronized (admissionMonitor) {
            Instant now = Instant.now(clock);
            Instant oldestUsernameAttempt = now.minus(usernameWindow);
            Instant oldestAddressAttempt = now.minus(addressWindow);
            String sourceAddressKey = addressKey(sourceAddress);
            Attempts usernameAttempts = usernames.getIfPresent(canonicalUsername);
            Attempts addressAttempts = addresses.getIfPresent(sourceAddressKey);

            int usernameCount = usernameAttempts == null ? 0 : usernameAttempts.countSince(oldestUsernameAttempt);
            int addressCount = addressAttempts == null ? 0 : addressAttempts.countSince(oldestAddressAttempt);
            if (usernameCount >= usernameMaximum || addressCount >= addressMaximum) {
                return false;
            }

            if (usernameAttempts == null) {
                usernameAttempts = new Attempts();
                usernames.put(canonicalUsername, usernameAttempts);
            }
            if (addressAttempts == null) {
                addressAttempts = new Attempts();
                addresses.put(sourceAddressKey, addressAttempts);
            }
            usernameAttempts.add(now);
            addressAttempts.add(now);
            return true;
        }
    }

    public void recordSuccess(String canonicalUsername) {
        synchronized (admissionMonitor) {
            usernames.invalidate(canonicalUsername);
        }
    }

    long usernameCacheSize() {
        synchronized (admissionMonitor) {
            usernames.cleanUp();
            return usernames.estimatedSize();
        }
    }

    long addressCacheSize() {
        synchronized (admissionMonitor) {
            addresses.cleanUp();
            return addresses.estimatedSize();
        }
    }

    private static Cache<String, Attempts> newCache(long maximumSize, Duration retention) {
        return Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(retention)
                .build();
    }

    private String addressKey(String address) {
        return address == null || address.isBlank() ? "unknown" : address;
    }

    private void validate(SecurityProperties.LoginRateLimit config) {
        if (config.getUsernameMaxAttempts() < 1 || config.getAddressMaxAttempts() < 1
                || config.getMaximumSize() < 1
                || invalid(config.getUsernameWindow()) || invalid(config.getAddressWindow())) {
            throw new IllegalStateException("La configuration du limiteur de login doit être positive et bornée");
        }
    }

    private boolean invalid(Duration value) {
        return value == null || value.isZero() || value.isNegative();
    }

    private static final class Attempts {
        private final Deque<Instant> instants = new ArrayDeque<>();

        void add(Instant now) {
            instants.addLast(now);
        }

        int countSince(Instant oldestAllowed) {
            prune(oldestAllowed);
            return instants.size();
        }

        private void prune(Instant oldestAllowed) {
            while (!instants.isEmpty() && instants.peekFirst().isBefore(oldestAllowed)) {
                instants.removeFirst();
            }
        }
    }
}
