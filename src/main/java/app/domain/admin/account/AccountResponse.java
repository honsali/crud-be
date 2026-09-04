package app.domain.admin.account;

import app.core.reference.JsonId;
import app.core.reference.Reference;

public record AccountResponse(
        @JsonId Long id,
        String username,
        Reference role,
        boolean activated,
        long version) {
}
