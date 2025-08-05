package app.core.validation;

public class NullElementInCollectionException extends AssertionException {

    private static String message(String field) {
        return new StringBuilder().append("The field \"").append(field).append("\" contains a null element").toString();
    }

    public NullElementInCollectionException(String field) {
        super(field, message(field));
    }

    @Override
    public AssertionErrorType type() {
        return AssertionErrorType.NULL_ELEMENT_IN_COLLECTION;
    }
}
