package app.core.security.credential;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import app.core.exception.ConflictException;
import app.core.security.authentication.InvalidCredentialsException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CredentialService {

    private final AccountCredentialRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public CredentialService(
            AccountCredentialRepository repository,
            PasswordEncoder passwordEncoder,
            Clock clock) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Optional<LoginCredentialSnapshot> findForLogin(Long accountId) {
        return repository.findLoginCredentialByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public Optional<Long> findTokenVersion(Long accountId) {
        return repository.findTokenVersionByAccountId(accountId);
    }

    @Transactional
    public void createInitialCredential(Long accountId, String password) {
        PasswordPolicy.requireValid(password);
        if (repository.existsByAccountId(accountId)) {
            throw new ConflictException("Une credential existe déjà pour ce compte");
        }
        repository.saveAndFlush(new AccountCredential(
                accountId,
                passwordEncoder.encode(password),
                Instant.now(clock)));
    }

    @Transactional
    public void changePassword(Long accountId, String currentPassword, String newPassword) {
        PasswordPolicy.requireValid(newPassword);
        AccountCredential credential = repository.findByAccountId(accountId)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(currentPassword, credential.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }
        credential.replacePassword(passwordEncoder.encode(newPassword), Instant.now(clock));
        repository.flush();
    }

    @Transactional
    public void resetPassword(Long accountId, String newPassword) {
        PasswordPolicy.requireValid(newPassword);
        AccountCredential credential = repository.findByAccountId(accountId).orElse(null);
        if (credential == null) {
            repository.saveAndFlush(new AccountCredential(
                    accountId,
                    passwordEncoder.encode(newPassword),
                    Instant.now(clock)));
            return;
        }
        credential.replacePassword(passwordEncoder.encode(newPassword), Instant.now(clock));
        repository.flush();
    }

    @Transactional
    public void revokeAllTokens(Long accountId) {
        AccountCredential credential = repository.findByAccountId(accountId)
                .orElseThrow(InvalidCredentialsException::new);
        credential.revokeAllTokens();
        repository.flush();
    }
}
