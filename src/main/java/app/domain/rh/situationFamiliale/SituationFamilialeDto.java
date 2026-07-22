package app.domain.rh.situationFamiliale;

import app.core.configuration.JsonId;
import jakarta.validation.constraints.NotBlank;

public record SituationFamilialeDto(
                @JsonId Long id,
                @JsonId Long idSituationFamiliale,
                @NotBlank String libelle) {
}
