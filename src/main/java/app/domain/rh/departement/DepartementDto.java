package app.domain.rh.departement;

import app.core.configuration.JsonId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartementDto(
        @JsonId Long id,
        @JsonId Long idDepartement,
        @NotBlank @Size(max = 250) String nom,
        String description) {
}
