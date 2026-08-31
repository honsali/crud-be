package app.core.security.jwt;

import java.time.Clock;
import java.time.Instant;

import app.core.security.config.SecurityProperties;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class RequiredJwtClaimsValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_TOKEN = new OAuth2Error(
            "invalid_token", "Le JWT ne respecte pas le contrat attendu", null);

    private final String audience;
    private final Clock clock;
    private final java.time.Duration clockSkew;

    public RequiredJwtClaimsValidator(SecurityProperties properties, Clock clock) {
        this.audience = properties.getJwt().getAudience();
        this.clock = clock;
        this.clockSkew = properties.getJwt().getClockSkew();
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (!validPositiveLong(token.getSubject())
                || token.getIssuedAt() == null
                || token.getNotBefore() == null
                || token.getExpiresAt() == null
                || token.getId() == null
                || token.getId().isBlank()
                || !validCredentialVersion(token.getClaim(JwtTokenService.CREDENTIAL_VERSION_CLAIM))
                || token.getAudience() == null
                || !token.getAudience().contains(audience)
                || token.getIssuedAt().isAfter(Instant.now(clock).plus(clockSkew))) {
            return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
        }
        return OAuth2TokenValidatorResult.success();
    }

    private boolean validPositiveLong(String value) {
        try {
            return value != null && Long.parseLong(value) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private boolean validCredentialVersion(Object value) {
        if (!(value instanceof Number number)) {
            return false;
        }
        long longValue = number.longValue();
        return longValue >= 0 && number.doubleValue() == (double) longValue;
    }
}
