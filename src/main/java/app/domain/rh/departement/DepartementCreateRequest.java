package app.domain.rh.departement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartementCreateRequest(
        @NotBlank @Size(max = 150) String nom,
        @Size(max = 1000) String description) {
}
