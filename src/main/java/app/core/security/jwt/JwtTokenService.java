package app.core.security.jwt;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import app.core.security.authentication.AuthenticatedAccountPrincipal;
import app.core.security.config.SecurityProperties;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    public static final String CREDENTIAL_VERSION_CLAIM = "credential_version";

    private final JwtEncoder encoder;
    private final SecurityProperties properties;
    private final Clock clock;

    public JwtTokenService(JwtEncoder encoder, SecurityProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    public JwtAccessTokenResponse issue(AuthenticatedAccountPrincipal principal) {
        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = issuedAt.plus(properties.getJwt().getTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getJwt().getIssuer())
                .audience(List.of(properties.getJwt().getAudience()))
                .issuedAt(issuedAt)
                .notBefore(issuedAt)
                .expiresAt(expiresAt)
                .subject(principal.accountId().toString())
                .id(UUID.randomUUID().toString())
                .claim(CREDENTIAL_VERSION_CLAIM, principal.tokenVersion())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new JwtAccessTokenResponse(
                token,
                "Bearer",
                properties.getJwt().getTtl().toSeconds());
    }
}
