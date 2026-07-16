package app.core.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class SecurityConfigurationTest {

    private final SecurityConfiguration configuration = new SecurityConfiguration();

    @Test
    void createsAnHs512KeyFromAValidSecret() {
        byte[] secret = new byte[64];
        String encodedSecret = Base64.getEncoder().encodeToString(secret);

        SecretKey key = configuration.jwtSecretKey(encodedSecret);

        assertThat(key.getAlgorithm()).isEqualTo("HmacSHA512");
        assertThat(key.getEncoded()).containsExactly(secret);
    }

    @Test
    void rejectsBlankMalformedAndShortSecrets() {
        assertThatThrownBy(() -> configuration.jwtSecretKey(" "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("APP_SECURITY_JWT_BASE64_SECRET must be configured.");
        assertThatThrownBy(() -> configuration.jwtSecretKey("not-base64"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret must be valid Base64.");
        assertThatThrownBy(() -> configuration.jwtSecretKey(
                Base64.getEncoder().encodeToString(new byte[63])))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT secret must decode to at least 64 bytes for HS512.");
    }
}
