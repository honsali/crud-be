package app.domain.admin.account;

import java.util.Arrays;

import app.core.exception.InvalidRequestException;

public enum AccountSortField {
    USERNAME("username", "username"),
    DISPLAY_NAME("displayName", "displayName"),
    EMAIL("email", "email"),
    ACTIVE("active", "active"),
    ROLE("role", "role.code");

    private final String apiValue;
    private final String property;

    AccountSortField(String apiValue, String property) {
        this.apiValue = apiValue;
        this.property = property;
    }

    public String property() {
        return property;
    }

    public static AccountSortField fromApiValue(String value) {
        return Arrays.stream(values())
                .filter(field -> field.apiValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException(
                        "Le tri doit être username, displayName, email, active ou role"));
    }
}
