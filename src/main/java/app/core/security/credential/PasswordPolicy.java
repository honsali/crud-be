package app.core.security.credential;

import app.core.exception.InvalidRequestException;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 15;
    public static final int MAX_LENGTH = 128;

    private PasswordPolicy() {
    }

    public static void requireValid(String password) {
        int length = password == null ? 0 : password.codePointCount(0, password.length());
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new InvalidRequestException("Le mot de passe doit contenir entre 15 et 128 caractères");
        }
    }
}
