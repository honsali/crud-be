package app.domain.rh.conge;

import java.time.LocalDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import app.core.reference.Reference;

public record CongeCreateRequest(
        @NotBlank @Size(max = 250) String code,
        @Valid Reference typeConge,
        LocalDate dateDebutConge,
        LocalDate dateFinConge,
        String commentaire) {
}
