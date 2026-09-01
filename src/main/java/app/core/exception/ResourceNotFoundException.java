package app.core.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object identifier) {
        super(resource + " introuvable avec l'identifiant " + identifier);
    }
}
