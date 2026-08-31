package app.domain.rh.employe;

import java.time.LocalDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import app.core.reference.Reference;

public record EmployeFiltre(
        @Size(max = 250) String matricule,
        @Size(max = 250) String nom,
        @Size(max = 250) String prenom,
        LocalDate debutDateNaissance,
        LocalDate finDateNaissance,
        @Valid Reference sexe,
        @Valid Reference situationFamiliale,
        LocalDate debutDateEntree,
        LocalDate finDateEntree,
        @Size(max = 250) String email,
        @Size(max = 250) String telephone,
        @Size(max = 250) String ville,
        @Size(max = 250) String adresse,
        @Size(max = 250) String fonction,
        String description,
        @Valid Reference departement) {
}
