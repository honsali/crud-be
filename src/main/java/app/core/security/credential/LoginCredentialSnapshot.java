package app.core.security.credential;

public record LoginCredentialSnapshot(Long accountId, String passwordHash, long tokenVersion) {
}
