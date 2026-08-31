package app.core.security.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();
    private final LoginRateLimit loginRateLimit = new LoginRateLimit();
    private final Bootstrap bootstrap = new Bootstrap();

    public Jwt getJwt() {
        return jwt;
    }

    public Cors getCors() {
        return cors;
    }

    public LoginRateLimit getLoginRateLimit() {
        return loginRateLimit;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public static class Jwt {
        private String secretBase64 = "";
        private String issuer = "crud-reference";
        private String audience = "crud-api";
        private Duration ttl = Duration.ofMinutes(15);
        private Duration clockSkew = Duration.ofSeconds(30);

        public String getSecretBase64() {
            return secretBase64;
        }

        public void setSecretBase64(String secretBase64) {
            this.secretBase64 = secretBase64;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(String audience) {
            this.audience = audience;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public Duration getClockSkew() {
            return clockSkew;
        }

        public void setClockSkew(Duration clockSkew) {
            this.clockSkew = clockSkew;
        }
    }

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins == null ? new ArrayList<>() : new ArrayList<>(allowedOrigins);
        }
    }

    public static class LoginRateLimit {
        private int usernameMaxAttempts = 5;
        private Duration usernameWindow = Duration.ofMinutes(15);
        private int addressMaxAttempts = 20;
        private Duration addressWindow = Duration.ofMinutes(15);
        private long maximumSize = 10_000;

        public int getUsernameMaxAttempts() {
            return usernameMaxAttempts;
        }

        public void setUsernameMaxAttempts(int usernameMaxAttempts) {
            this.usernameMaxAttempts = usernameMaxAttempts;
        }

        public Duration getUsernameWindow() {
            return usernameWindow;
        }

        public void setUsernameWindow(Duration usernameWindow) {
            this.usernameWindow = usernameWindow;
        }

        public int getAddressMaxAttempts() {
            return addressMaxAttempts;
        }

        public void setAddressMaxAttempts(int addressMaxAttempts) {
            this.addressMaxAttempts = addressMaxAttempts;
        }

        public Duration getAddressWindow() {
            return addressWindow;
        }

        public void setAddressWindow(Duration addressWindow) {
            this.addressWindow = addressWindow;
        }

        public long getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
        }
    }

    public static class Bootstrap {
        private boolean enabled;
        private String username = "";
        private String displayName = "";
        private String password = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
