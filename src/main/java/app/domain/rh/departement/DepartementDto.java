package app.domain.rh.departement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartementDto(
                Long id,
                Long idDepartement,
                @NotBlank @Size(max = 250) String nom,
                String description) {
}
