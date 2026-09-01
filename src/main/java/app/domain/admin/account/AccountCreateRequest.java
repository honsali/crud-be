package app.domain.admin.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import app.domain.admin.role.Role;

public record AccountCreateRequest(
        @NotBlank @Size(min = 3, max = 100) @Pattern(regexp = "[a-z0-9._-]+") String username,
        @NotBlank @Size(min = 8, max = 256) String password,
        @NotBlank @Pattern(regexp = "(?:ROLE_)?[A-Z][A-Z0-9_]{1,49}") String role) {

    public AccountCreateRequest {
        username = Account.normalizeUsername(username);
        role = Role.normalizeCode(role);
    }
}
