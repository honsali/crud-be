package app.domain.rh.employe;

import java.time.LocalDate;
import app.core.reference.JsonId;
import app.core.reference.Reference;

public record EmployeResponse(
        @JsonId Long id,
        String matricule,
        String nom,
        String prenom,
        LocalDate dateNaissance,
        Reference sexe,
        Reference situationFamiliale,
        LocalDate dateEntree,
        String email,
        String telephone,
        String ville,
        String adresse,
        String fonction,
        String description,
        Reference departement,
        long version) {
}
