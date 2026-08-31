package app.core.security.authentication;

public record AuthenticatedAccountPrincipal(
        Long accountId,
        String username,
        String roleCode,
        long tokenVersion) {
}
