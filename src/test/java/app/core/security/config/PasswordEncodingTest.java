package app.core.security.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncodingTest {

    private final PasswordEncoder encoder = new SecurityConfiguration().passwordEncoder();

    @Test
    void usesArgon2idWithARandomSalt() {
        String password = "mot de passe sûr";
        String first = encoder.encode(password);
        String second = encoder.encode(password);

        assertThat(first).startsWith("{argon2id}$argon2id$").isNotEqualTo(second);
        assertThat(encoder.matches(password, first)).isTrue();
        assertThat(encoder.matches(password, second)).isTrue();
    }
}
