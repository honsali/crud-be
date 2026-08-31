package app.domain.admin.role;

import app.core.reference.JsonId;

public record RoleReference(@JsonId Long id, String code, String libelle) {
}
