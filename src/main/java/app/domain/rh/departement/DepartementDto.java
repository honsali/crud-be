package app.domain.rh.departement;

import jakarta.validation.constraints.NotBlank;

public record DepartementDto(
        Long id,
        Long idDepartement,
        @NotBlank String nom,
        String description) {
}
