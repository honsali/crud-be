package app.core.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResult {

    public static final String TOKEN_PREFIX = "Bearer ";
    private String idToken;

    public AuthResult(String idToken) {
        this.idToken = idToken;
    }

    @JsonProperty("id_token")
    String getIdToken() {
        return idToken;
    }

    String getBearer() {
        return TOKEN_PREFIX + idToken;
    }
}
