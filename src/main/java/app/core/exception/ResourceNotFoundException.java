package app.core.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " introuvable avec l'identifiant " + id);
    }
}
