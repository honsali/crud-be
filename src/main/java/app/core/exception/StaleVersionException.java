package app.core.exception;

public class StaleVersionException extends ConflictException {

    public StaleVersionException(String resource, Long id) {
        super(resource + " " + id + " a été modifié depuis sa lecture");
    }
}
