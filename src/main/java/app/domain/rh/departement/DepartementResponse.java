package app.domain.rh.departement;

import app.core.reference.JsonId;

public record DepartementResponse(
        @JsonId Long id,
        String nom,
        String description,
        long version) {
}
