package app.core.security.web;

import app.core.reference.JsonId;

public record CurrentAccountResponse(@JsonId Long accountId, String username, String roleCode) {
}
