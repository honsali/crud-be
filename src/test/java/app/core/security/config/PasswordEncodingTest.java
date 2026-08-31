package app.core.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.core.exception.InvalidRequestException;
import app.core.security.credential.PasswordPolicy;

import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncodingTest {

    private final PasswordEncoder encoder = new SecurityBeansConfiguration().passwordEncoder();

    @Test
    void argon2idUsesARandomSaltAndDelegatingPrefix() {
        String password = "mot de passe suffisamment sûr";
        String first = encoder.encode(password);
        String second = encoder.encode(password);

        assertThat(first).startsWith("{argon2id}$argon2id$").isNotEqualTo(second);
        assertThat(encoder.matches(password, first)).isTrue();
        assertThat(encoder.matches(password, second)).isTrue();
    }

    @Test
    void leadingAndTrailingSpacesArePartOfThePassword() {
        String password = " mot de passe Unicode sûr ";
        String encoded = encoder.encode(password);

        assertThat(encoder.matches(password, encoded)).isTrue();
        assertThat(encoder.matches(password.strip(), encoded)).isFalse();
    }

    @Test
    void unicodeLengthIsMeasuredWithoutNormalizationOrUtf16Distortion() {
        assertThatCode(() -> PasswordPolicy.requireValid("😀".repeat(15))).doesNotThrowAnyException();
        assertThatCode(() -> PasswordPolicy.requireValid("😀".repeat(128))).doesNotThrowAnyException();
        assertThatThrownBy(() -> PasswordPolicy.requireValid("😀".repeat(129)))
                .isInstanceOf(InvalidRequestException.class);
    }
}
