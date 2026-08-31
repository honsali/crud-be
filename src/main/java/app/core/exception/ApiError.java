package app.core.exception;

import java.util.List;

public record ApiError(
        String code,
        String message,
        String path,
        List<FieldViolation> fieldErrors) {

    public ApiError {
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }
}
