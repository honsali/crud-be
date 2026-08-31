package app.domain.admin.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleCreateRequest(
        @NotBlank @Size(min = 2, max = 50) @Pattern(regexp = "[A-Z][A-Z0-9_]*") String code,
        @NotBlank @Size(max = 150) String libelle,
        @Size(max = 1000) String description) {

    public RoleCreateRequest {
        code = Role.normalizeCode(code);
        libelle = Role.normalizeLibelle(libelle);
    }
}
