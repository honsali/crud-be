package app.domain.admin.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import app.core.reference.Reference;

public record AccountUpdateRequest(
        @NotNull @Valid Reference role,
        @NotNull Boolean activated) {
}
