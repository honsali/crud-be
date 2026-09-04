package app.domain.admin.account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import app.core.reference.Reference;

public record AccountUpdateRequest(
        @NotNull @Valid Reference role,
        @NotNull Boolean activated,
        @NotNull @PositiveOrZero Long version) {
}
