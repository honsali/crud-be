package app.domain.rh.sexe;

import jakarta.validation.constraints.NotBlank;

public record SexeDto(
                Long id,
                Long idSexe,
                @NotBlank String libelle,
                @NotBlank String code) {
}
