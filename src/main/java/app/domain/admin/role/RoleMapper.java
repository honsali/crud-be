package app.domain.admin.role;

import app.core.reference.Reference;

public final class RoleMapper {

    public static Reference toReference(Role role) {
        return new Reference(role.getId(), role.getLibelle());
    }

    private RoleMapper() {}
}
