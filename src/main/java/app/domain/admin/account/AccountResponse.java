package app.domain.admin.account;

import app.core.reference.JsonId;
import app.domain.admin.role.RoleReference;

public record AccountResponse(
                @JsonId Long id,
                String username,
                String displayName,
                String email,
                boolean active,
                RoleReference role,
                long version) {
}
