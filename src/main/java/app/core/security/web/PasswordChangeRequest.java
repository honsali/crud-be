package app.core.security.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotNull @Size(max = 256) String currentPassword,
        @NotNull @Size(max = 256) String newPassword) {
}
