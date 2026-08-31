package app.core.security.jwt;

public record JwtAccessTokenResponse(String accessToken, String tokenType, long expiresIn) {
}
