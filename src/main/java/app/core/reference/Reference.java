package app.core.reference;

import jakarta.validation.constraints.NotNull;

public record Reference(@NotNull @JsonId Long id, String libelle) {
}
