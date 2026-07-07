package app.domain.rh.employe;

import java.time.LocalDate;
import app.domain.rh.departement.DepartementDto;
import app.domain.rh.sexe.SexeDto;
import app.domain.rh.situationFamiliale.SituationFamilialeDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmployeDto(
        Long id,
        Long idEmploye,
        @NotBlank String matricule,
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotNull LocalDate dateNaissance,
        SexeDto sexe,
        SituationFamilialeDto situationFamiliale,
        LocalDate dateEntree,
        String email,
        String telephone,
        String ville,
        String adresse,
        String fonction,
        String description,
        DepartementDto departement) {
}
