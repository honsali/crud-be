package app.domain.rh.conge;

import java.time.LocalDate;
import app.core.reference.JsonId;
import app.core.reference.Reference;

public record CongeResponse(
        @JsonId Long id,
        String code,
        Reference typeConge,
        LocalDate dateDebutConge,
        LocalDate dateFinConge,
        String commentaire,
        Reference employe,
        long version) {
}
