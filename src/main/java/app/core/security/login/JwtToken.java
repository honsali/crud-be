package app.core.security.login;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import app.core.security.account.AppRole;

public final class JwtToken {

    public static final String ROLE_CLAIM = "role";
    public static final String ACCOUNT_ID_CLAIM = "aid";
    public static final String TOKEN_VERSION_CLAIM = "ver";
    public static final MacAlgorithm ALGORITHM = MacAlgorithm.HS512;

    public static Optional<AppRole> role(Jwt jwt) {
        Object value = jwt.getClaim(ROLE_CLAIM);
        if (!(value instanceof String role)) {
            return Optional.empty();
        }
        return AppRole.fromAuthority(role);
    }

    public static Optional<Long> accountId(Jwt jwt) {
        return exactLongClaim(jwt, ACCOUNT_ID_CLAIM, 1L);
    }

    public static Optional<Long> tokenVersion(Jwt jwt) {
        return exactLongClaim(jwt, TOKEN_VERSION_CLAIM, 0L);
    }

    private static Optional<Long> exactLongClaim(Jwt jwt, String claimName, long minimum) {
        Object value = jwt.getClaim(claimName);
        long result;
        try {
            if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
                result = ((Number) value).longValue();
            } else if (value instanceof BigInteger integer) {
                result = integer.longValueExact();
            } else if (value instanceof BigDecimal decimal && decimal.scale() <= 0) {
                result = decimal.longValueExact();
            } else {
                return Optional.empty();
            }
        } catch (ArithmeticException exception) {
            return Optional.empty();
        }
        return result >= minimum ? Optional.of(result) : Optional.empty();
    }

    private JwtToken() {}
}
