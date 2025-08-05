package app.core.validation;

import java.util.Map;

public class TooManyElementsException extends AssertionException {

    public static class TooManyElementsExceptionBuilder {

        private String field;
        private int maxSize;
        private int size;

        public TooManyElementsExceptionBuilder field(String field) {
            this.field = field;

            return this;
        }

        public TooManyElementsExceptionBuilder maxSize(int maxSize) {
            this.maxSize = maxSize;

            return this;
        }

        public TooManyElementsExceptionBuilder size(int size) {
            this.size = size;

            return this;
        }

        public TooManyElementsException build() {
            return new TooManyElementsException(this);
        }

        private String message() {
            return new StringBuilder().append("Size of collection \"").append(field).append("\" must be at most ").append(maxSize).append(" but was ").append(size).toString();
        }
    }

    public static TooManyElementsExceptionBuilder builder() {
        return new TooManyElementsExceptionBuilder();
    }

    private final String maxSize;

    private final String currentSize;

    public TooManyElementsException(TooManyElementsExceptionBuilder builder) {
        super(builder.field, builder.message());
        maxSize = String.valueOf(builder.maxSize);
        currentSize = String.valueOf(builder.size);
    }

    @Override
    public AssertionErrorType type() {
        return AssertionErrorType.TOO_MANY_ELEMENTS;
    }

    @Override
    public Map<String, String> parameters() {
        return Map.of("maxSize", maxSize, "currentSize", currentSize);
    }
}
