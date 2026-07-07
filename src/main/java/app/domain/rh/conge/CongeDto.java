package app.domain.rh.conge;

import java.time.LocalDate;
import app.domain.rh.employe.EmployeDto;
import app.domain.rh.typeConge.TypeCongeDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CongeDto(
        Long id,
        Long idConge,
        @NotBlank String code,
        TypeCongeDto typeConge,
        LocalDate dateDebutConge,
        LocalDate dateFinConge,
        String commentaire,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) EmployeDto employe) {
}
