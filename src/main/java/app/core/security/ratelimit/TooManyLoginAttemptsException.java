package app.core.security.ratelimit;

public class TooManyLoginAttemptsException extends RuntimeException {

    public TooManyLoginAttemptsException() {
        super("Trop de tentatives de connexion");
    }
}
