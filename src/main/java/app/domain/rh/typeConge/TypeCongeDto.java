package app.domain.rh.typeConge;

import jakarta.validation.constraints.NotBlank;

public record TypeCongeDto(
                Long id,
                Long idTypeConge,
        @NotBlank String libelle) {
}
