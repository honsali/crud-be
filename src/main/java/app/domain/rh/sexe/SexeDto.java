package app.domain.rh.sexe;

import app.core.configuration.JsonId;
import jakarta.validation.constraints.NotBlank;

public record SexeDto(
        @JsonId Long id,
        @JsonId Long idSexe,
        @NotBlank String libelle) {
}
