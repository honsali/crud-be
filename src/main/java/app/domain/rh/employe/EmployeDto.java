package app.domain.rh.employe;

import java.time.LocalDate;
import app.domain.rh.departement.DepartementDto;
import app.domain.rh.sexe.SexeDto;
import app.domain.rh.situationFamiliale.SituationFamilialeDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeDto(
                Long id,
                Long idEmploye,
                @NotBlank @Size(max = 250) String matricule,
                @NotBlank @Size(max = 250) String nom,
                @NotBlank @Size(max = 250) String prenom,
                @NotNull LocalDate dateNaissance,
                SexeDto sexe,
                SituationFamilialeDto situationFamiliale,
                LocalDate dateEntree,
                @Size(max = 250) String email,
                @Size(max = 250) String telephone,
                @Size(max = 250) String ville,
                @Size(max = 250) String adresse,
                @Size(max = 250) String fonction,
                String description,
                DepartementDto departement) {
}
