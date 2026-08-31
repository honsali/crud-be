package app.core.exception;

public class FieldConflictException extends ConflictException {

    public FieldConflictException(String entity, String fieldName, String fieldValue) {
        super(entity + " existe déjà avec " + fieldName + " : " + fieldValue);
    }
}
