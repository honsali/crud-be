package app.domain.admin.role;

import app.core.reference.JsonId;

public record RoleResponse(
                @JsonId Long id,
                String code,
                String libelle,
                String description,
                long version) {
}
