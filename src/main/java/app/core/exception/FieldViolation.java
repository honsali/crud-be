package app.core.exception;

public record FieldViolation(String field, String code, String message) {
}
