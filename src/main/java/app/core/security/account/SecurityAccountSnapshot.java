package app.core.security.account;

public record SecurityAccountSnapshot(
        Long accountId,
        String username,
        boolean active,
        String roleCode) {
}
