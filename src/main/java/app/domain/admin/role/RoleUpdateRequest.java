package app.domain.admin.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record RoleUpdateRequest(
        @NotBlank @Size(max = 150) String libelle,
        @Size(max = 1000) String description,
        @NotNull @PositiveOrZero Long version) {

    public RoleUpdateRequest {
        libelle = Role.normalizeLibelle(libelle);
    }
}
