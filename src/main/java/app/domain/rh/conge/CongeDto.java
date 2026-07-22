package app.domain.rh.conge;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import app.core.configuration.JsonId;
import app.domain.rh.employe.EmployeDto;
import app.domain.rh.typeConge.TypeCongeDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CongeDto(
        @JsonId Long id,
        @JsonId Long idConge,
        @NotBlank @Size(max = 250) String code,
        TypeCongeDto typeConge,
        LocalDate dateDebutConge,
        LocalDate dateFinConge,
        String commentaire,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) EmployeDto employe) {
}
