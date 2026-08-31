package app.core.security.credential;

public class InvalidCurrentPasswordException extends RuntimeException {

    public InvalidCurrentPasswordException() {
        super("Le mot de passe actuel est incorrect");
    }
}
