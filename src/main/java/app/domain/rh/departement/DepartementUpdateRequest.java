package app.domain.rh.departement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record DepartementUpdateRequest(
        @NotBlank @Size(max = 150) String nom,
        @Size(max = 1000) String description,
        @NotNull @PositiveOrZero Long version) {
}
