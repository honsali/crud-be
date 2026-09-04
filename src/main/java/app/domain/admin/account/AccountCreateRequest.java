package app.domain.admin.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import app.core.reference.Reference;

public record AccountCreateRequest(
        @NotBlank @Size(min = 3, max = 100) @Pattern(regexp = "\\s*[A-Za-z0-9._-]{3,100}\\s*") String username,
        @NotBlank @Size(min = 8, max = 256) String password,
        @NotNull @Valid Reference role) {
}
