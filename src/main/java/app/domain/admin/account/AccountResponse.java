package app.domain.admin.account;

import app.core.reference.JsonId;
import app.domain.admin.role.RoleReference;

public record AccountResponse(
        @JsonId Long id,
        String username,
        RoleReference role,
        boolean activated) {
}
