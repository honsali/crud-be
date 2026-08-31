package app.core.security.credential;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    @Mock AccountCredentialRepository repository;
    @Mock PasswordEncoder passwordEncoder;
    private CredentialService service;

    @BeforeEach
    void setUp() {
        service = new CredentialService(
                repository,
                passwordEncoder,
                Clock.fixed(Instant.parse("2026-08-11T10:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void wrongCurrentPasswordUsesItsDedicatedExceptionBeforeEncodingTheNewOne() {
        AccountCredential credential = new AccountCredential(
                7L, "{argon2id}stored-hash", Instant.parse("2026-08-10T10:00:00Z"));
        when(repository.findByAccountId(7L)).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("wrong-current-password", "{argon2id}stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(
                7L, "wrong-current-password", "nouveau mot de passe très sûr"))
                .isInstanceOf(InvalidCurrentPasswordException.class)
                .hasMessage("Le mot de passe actuel est incorrect");

        verify(passwordEncoder).matches("wrong-current-password", "{argon2id}stored-hash");
        verify(passwordEncoder, never()).encode("nouveau mot de passe très sûr");
    }
}
