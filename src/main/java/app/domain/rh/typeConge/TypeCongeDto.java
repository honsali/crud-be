package app.domain.rh.typeConge;

import app.core.configuration.JsonId;
import jakarta.validation.constraints.NotBlank;

public record TypeCongeDto(
                @JsonId Long id,
                @JsonId Long idTypeConge,
                @NotBlank String libelle) {
}
