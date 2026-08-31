package app.domain.admin.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AccountUpdateRequest(
        @NotBlank @Size(min = 3, max = 100) @Pattern(regexp = "[a-z0-9._-]+") String username,
        @NotBlank @Size(max = 150) String displayName,
        @Email @Size(max = 254) String email,
        @NotNull Boolean active,
        @NotNull Long roleId,
        @NotNull @PositiveOrZero Long version) {

    public AccountUpdateRequest {
        username = Account.normalizeUsername(username);
        displayName = Account.normalizeDisplayName(displayName);
        email = Account.normalizeEmail(email);
    }
}
