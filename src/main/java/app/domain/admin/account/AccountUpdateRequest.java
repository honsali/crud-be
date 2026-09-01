package app.domain.admin.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import app.domain.admin.role.Role;

public record AccountUpdateRequest(
        @NotBlank @Pattern(regexp = "(?:ROLE_)?[A-Z][A-Z0-9_]{1,49}") String role,
        @NotNull Boolean activated) {

    public AccountUpdateRequest {
        role = Role.normalizeCode(role);
    }
}
