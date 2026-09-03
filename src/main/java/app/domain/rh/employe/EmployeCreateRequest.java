package app.domain.rh.employe;

import java.time.LocalDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import app.core.reference.Reference;

public record EmployeCreateRequest(
        @NotBlank @Size(max = 250) String matricule,
        LocalDate dateEntree,
        @Valid Reference departement,
        @Size(max = 250) String fonction,
        String description,
        @NotBlank @Size(max = 250) String nom,
        @NotBlank @Size(max = 250) String prenom,
        @NotNull LocalDate dateNaissance,
        @Valid Reference sexe,
        @Valid Reference situationFamiliale,
        @Size(max = 250) String email,
        @Size(max = 250) String telephone,
        @Size(max = 250) String ville,
        @Size(max = 250) String adresse) {
}
