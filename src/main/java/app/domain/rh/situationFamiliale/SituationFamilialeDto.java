package app.domain.rh.situationFamiliale;

import jakarta.validation.constraints.NotBlank;

public record SituationFamilialeDto(
        Long id,
        Long idSituationFamiliale,
        @NotBlank String libelle,
        @NotBlank String code) {
}
