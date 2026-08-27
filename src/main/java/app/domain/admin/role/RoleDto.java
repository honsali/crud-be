package app.domain.admin.role;

import app.core.configuration.JsonId;
import jakarta.validation.constraints.NotBlank;

public record RoleDto(
        @JsonId Long id,
        @JsonId Long idRole,
        @NotBlank String libelle) {
}
