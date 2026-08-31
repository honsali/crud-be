package app.core.security.config;

import java.time.Clock;
import java.time.Duration;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import app.core.security.jwt.RequiredJwtClaimsValidator;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfiguration {

    @Bean
    SecretKey jwtSecretKey(SecurityProperties properties) {
        String configuredSecret = properties.getJwt().getSecretBase64();
        if (configuredSecret == null || configuredSecret.isBlank()) {
            throw new IllegalStateException("APP_SECURITY_JWT_SECRET_BASE64 est obligatoire");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(configuredSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("APP_SECURITY_JWT_SECRET_BASE64 doit être un Base64 valide", exception);
        }
        if (decoded.length < 32) {
            throw new IllegalStateException(
                    "APP_SECURITY_JWT_SECRET_BASE64 doit représenter au moins 32 octets");
        }
        validateJwtProperties(properties);
        return new SecretKeySpec(decoded, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey secretKey, SecurityProperties properties, Clock clock) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtTimestampValidator timestamps = new JwtTimestampValidator(properties.getJwt().getClockSkew());
        timestamps.setClock(clock);
        OAuth2TokenValidator<Jwt> validators = new DelegatingOAuth2TokenValidator<>(
                timestamps,
                new JwtIssuerValidator(properties.getJwt().getIssuer()),
                new RequiredJwtClaimsValidator(properties, clock));
        decoder.setJwtValidator(validators);
        return decoder;
    }

    private void validateJwtProperties(SecurityProperties properties) {
        if (properties.getJwt().getIssuer() == null || properties.getJwt().getIssuer().isBlank()) {
            throw new IllegalStateException("L'issuer JWT ne doit pas être vide");
        }
        if (properties.getJwt().getAudience() == null || properties.getJwt().getAudience().isBlank()) {
            throw new IllegalStateException("L'audience JWT ne doit pas être vide");
        }
        Duration ttl = properties.getJwt().getTtl();
        if (ttl == null || ttl.compareTo(Duration.ofSeconds(1)) < 0 || ttl.compareTo(Duration.ofHours(1)) > 0) {
            throw new IllegalStateException("Le TTL JWT doit être positif et inférieur ou égal à PT1H");
        }
        Duration clockSkew = properties.getJwt().getClockSkew();
        if (clockSkew == null || clockSkew.isNegative() || clockSkew.compareTo(Duration.ofMinutes(1)) > 0) {
            throw new IllegalStateException("La tolérance d'horloge JWT doit être comprise entre PT0S et PT1M");
        }
    }
}
