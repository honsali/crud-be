package app.core.security.account;

import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AppRole {
    ROLE_ADMIN, ROLE_GESTIONNAIRE_RH;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AppRole fromJson(Object value) {
        if (!(value instanceof String authority)) {
            throw new IllegalArgumentException("Role must be a canonical string value.");
        }
        return fromAuthority(authority).orElseThrow(() -> new IllegalArgumentException("Unknown application role: " + authority));
    }

    public static Optional<AppRole> fromAuthority(String authority) {
        if (authority == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(authority));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
