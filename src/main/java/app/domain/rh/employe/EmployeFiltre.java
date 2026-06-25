package app.domain.rh.employe;

import java.time.LocalDate;
import app.domain.rh.departement.DepartementDto;
import app.domain.rh.sexe.SexeDto;
import app.domain.rh.situationFamiliale.SituationFamilialeDto;

public record EmployeFiltre(
        String matricule,
        String nom,
        String prenom,
        LocalDate debutDateNaissance,
        LocalDate finDateNaissance,
        SexeDto sexe,
        SituationFamilialeDto situationFamiliale,
        LocalDate debutDateEntree,
        LocalDate finDateEntree,
        String email,
        String telephone,
        String ville,
        String adresse,
        String fonction,
        String description,
        DepartementDto departement) {
}
