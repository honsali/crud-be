package app.domain.rh.conge;

import java.time.LocalDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import app.core.reference.Reference;

public record CongeUpdateRequest(
        @NotBlank @Size(max = 250) String code,
        @Valid Reference typeConge,
        LocalDate dateDebutConge,
        LocalDate dateFinConge,
        String commentaire,
        @NotNull @PositiveOrZero Long version) {
}
