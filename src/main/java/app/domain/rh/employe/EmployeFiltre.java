package app.domain.rh.employe;

import java.time.LocalDate;
import app.domain.rh.departement.DepartementDto;
import app.domain.rh.sexe.SexeDto;
import app.domain.rh.situationFamiliale.SituationFamilialeDto;
import jakarta.validation.constraints.Size;

public record EmployeFiltre(
        @Size(max = 250) String matricule,
        @Size(max = 250) String nom,
        @Size(max = 250) String prenom,
        LocalDate debutDateNaissance,
        LocalDate finDateNaissance,
        SexeDto sexe,
        SituationFamilialeDto situationFamiliale,
        LocalDate debutDateEntree,
        LocalDate finDateEntree,
        @Size(max = 250) String email,
        @Size(max = 250) String telephone,
        @Size(max = 250) String ville,
        @Size(max = 250) String adresse,
        @Size(max = 250) String fonction,
        String description,
        DepartementDto departement) {
}
