package app.core.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import app.core.security.authentication.AuthenticatedAccountPrincipal;
import app.core.security.jwt.JwtAccessTokenResponse;
import app.core.security.jwt.JwtTokenService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtSecurityTest {

    private static final Instant NOW = Instant.parse("2026-08-10T12:00:00Z");
    private SecurityProperties properties;
    private Clock clock;
    private SecretKey key;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        properties = new SecurityProperties();
        byte[] secret = "0123456789012345678901234567890123456789012345678901234567890123"
                .getBytes(StandardCharsets.US_ASCII);
        properties.getJwt().setSecretBase64(Base64.getEncoder().encodeToString(secret));
        properties.getJwt().setIssuer("issuer-test");
        properties.getJwt().setAudience("audience-test");
        properties.getJwt().setTtl(Duration.ofMinutes(15));
        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        JwtConfiguration configuration = new JwtConfiguration();
        key = configuration.jwtSecretKey(properties);
        decoder = configuration.jwtDecoder(key, properties, clock);
    }

    @Test
    void issuesEveryRequiredClaimWithDeterministicExpirationAndNoProfileData() {
        JwtConfiguration configuration = new JwtConfiguration();
        JwtTokenService service = new JwtTokenService(configuration.jwtEncoder(key), properties, clock);
        JwtAccessTokenResponse response = service.issue(new AuthenticatedAccountPrincipal(7L, "alice", "ADMIN", 4));
        JwtAccessTokenResponse secondResponse = service.issue(
                new AuthenticatedAccountPrincipal(7L, "alice", "ADMIN", 4));
        Jwt jwt = decoder.decode(response.accessToken());
        Jwt secondJwt = decoder.decode(secondResponse.accessToken());

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900);
        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("issuer-test");
        assertThat(jwt.getAudience()).containsExactly("audience-test");
        assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
        assertThat(jwt.getNotBefore()).isEqualTo(NOW);
        assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plusSeconds(900));
        assertThat(jwt.getId()).isNotBlank();
        assertThat(secondJwt.getId()).isNotEqualTo(jwt.getId());
        assertThat(jwt.<Number>getClaim(JwtTokenService.CREDENTIAL_VERSION_CLAIM).longValue()).isEqualTo(4);
        assertThat(jwt.getClaims()).doesNotContainKeys("role", "roleCode", "email", "displayName", "username");
    }

    @Test
    void rejectsAlteredSignatureWrongKeyNoneAndEveryNonHs256Algorithm() {
        String valid = encode(key, MacAlgorithm.HS256, builder -> {});
        char replacement = valid.endsWith("A") ? 'Q' : 'A';
        String altered = valid.substring(0, valid.length() - 1) + replacement;
        assertDecodingRejected(altered);

        SecretKey wrongKey = new SecretKeySpec("x".repeat(64).getBytes(StandardCharsets.US_ASCII), "HmacSHA256");
        assertDecodingRejected(encode(wrongKey, MacAlgorithm.HS256, builder -> {}));
        assertDecodingRejected(encode(key, MacAlgorithm.HS384, builder -> {}));
        assertDecodingRejected(encode(key, MacAlgorithm.HS512, builder -> {}));

        String noneHeader = base64Url("{\"alg\":\"none\"}");
        String nonePayload = base64Url("{\"sub\":\"7\",\"exp\":9999999999}");
        assertDecodingRejected(noneHeader + "." + nonePayload + ".");
    }

    @Test
    void rejectsMissingOrIncorrectIssuerAudienceAndRequiredClaims() {
        List<String> invalidTokens = List.of(
                encodeClaims(baseClaims().issuer("wrong-issuer")),
                encodeClaims(baseClaimsWithoutIssuer()),
                encodeClaims(baseClaims().audience(List.of("wrong-audience"))),
                encodeClaims(baseClaimsWithoutAudience()),
                encodeClaims(baseClaimsWithoutExpiration()),
                encodeClaims(baseClaimsWithoutSubject()),
                encodeClaims(baseClaimsWithoutCredentialVersion()),
                encodeClaims(baseClaimsWithoutIssuedAt()),
                encodeClaims(baseClaimsWithoutNotBefore()),
                encodeClaims(baseClaimsWithoutId()));
        invalidTokens.forEach(this::assertValidationRejected);
    }

    @Test
    void rejectsMalformedSubjectsAndInvalidTimeWindows() {
        for (String subject : List.of("not-a-number", "-1", "0", "9223372036854775808")) {
            assertValidationRejected(encodeClaims(baseClaims().subject(subject)));
        }
        assertValidationRejected(encodeClaims(baseClaims()
                .issuedAt(NOW.minus(Duration.ofHours(1)))
                .notBefore(NOW.minus(Duration.ofHours(1)))
                .expiresAt(NOW.minusSeconds(31))));
        assertValidationRejected(encodeClaims(baseClaims()
                .issuedAt(NOW.plusSeconds(31))
                .notBefore(NOW.plusSeconds(31))
                .expiresAt(NOW.plus(Duration.ofMinutes(16)))));
    }

    @Test
    void refusesMissingInvalidAndTooShortSecretsAndInvalidJwtSettings() {
        JwtConfiguration configuration = new JwtConfiguration();
        properties.getJwt().setSecretBase64("");
        assertThatThrownBy(() -> configuration.jwtSecretKey(properties)).isInstanceOf(IllegalStateException.class);
        properties.getJwt().setSecretBase64("not-base64!");
        assertThatThrownBy(() -> configuration.jwtSecretKey(properties)).isInstanceOf(IllegalStateException.class);
        properties.getJwt().setSecretBase64(Base64.getEncoder().encodeToString(new byte[31]));
        assertThatThrownBy(() -> configuration.jwtSecretKey(properties)).isInstanceOf(IllegalStateException.class);

        properties.getJwt().setSecretBase64(Base64.getEncoder().encodeToString(new byte[32]));
        properties.getJwt().setIssuer(" ");
        assertThatThrownBy(() -> configuration.jwtSecretKey(properties)).isInstanceOf(IllegalStateException.class);
        properties.getJwt().setIssuer("issuer-test");
        properties.getJwt().setAudience("");
        assertThatThrownBy(() -> configuration.jwtSecretKey(properties)).isInstanceOf(IllegalStateException.class);
        properties.getJwt().setAudience("audience-test");
        properties.getJwt().setTtl(Duration.ofHours(2));
        assertThatThrownBy(() -> configuration.jwtSecretKey(properties)).isInstanceOf(IllegalStateException.class);
        properties.getJwt().setTtl(Duration.ofMinutes(15));
        properties.getJwt().setClockSkew(Duration.ofMinutes(2));
        assertThatThrownBy(() -> configuration.jwtSecretKey(properties)).isInstanceOf(IllegalStateException.class);
    }

    private String encode(SecretKey signingKey, MacAlgorithm algorithm, Consumer<JwtClaimsSet.Builder> customization) {
        JwtClaimsSet.Builder builder = baseClaims();
        customization.accept(builder);
        return new NimbusJwtEncoder(new ImmutableSecret<>(signingKey)).encode(JwtEncoderParameters.from(
                JwsHeader.with(algorithm).type("JWT").build(), builder.build())).getTokenValue();
    }

    private String encodeClaims(JwtClaimsSet.Builder claims) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key)).encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims.build())).getTokenValue();
    }

    private JwtClaimsSet.Builder baseClaims() {
        return JwtClaimsSet.builder()
                .issuer("issuer-test")
                .audience(List.of("audience-test"))
                .issuedAt(NOW)
                .notBefore(NOW)
                .expiresAt(NOW.plus(Duration.ofMinutes(15)))
                .subject("7")
                .id(UUID.randomUUID().toString())
                .claim(JwtTokenService.CREDENTIAL_VERSION_CLAIM, 0L);
    }

    private JwtClaimsSet.Builder baseClaimsWithoutIssuer() {
        return JwtClaimsSet.builder().audience(List.of("audience-test")).issuedAt(NOW).notBefore(NOW)
                .expiresAt(NOW.plusSeconds(900)).subject("7").id(UUID.randomUUID().toString())
                .claim(JwtTokenService.CREDENTIAL_VERSION_CLAIM, 0L);
    }

    private JwtClaimsSet.Builder baseClaimsWithoutAudience() {
        return JwtClaimsSet.builder().issuer("issuer-test").issuedAt(NOW).notBefore(NOW)
                .expiresAt(NOW.plusSeconds(900)).subject("7").id(UUID.randomUUID().toString())
                .claim(JwtTokenService.CREDENTIAL_VERSION_CLAIM, 0L);
    }

    private JwtClaimsSet.Builder baseClaimsWithoutExpiration() {
        return JwtClaimsSet.builder().issuer("issuer-test").audience(List.of("audience-test"))
                .issuedAt(NOW).notBefore(NOW).subject("7").id(UUID.randomUUID().toString())
                .claim(JwtTokenService.CREDENTIAL_VERSION_CLAIM, 0L);
    }

    private JwtClaimsSet.Builder baseClaimsWithoutSubject() {
        return JwtClaimsSet.builder().issuer("issuer-test").audience(List.of("audience-test"))
                .issuedAt(NOW).notBefore(NOW).expiresAt(NOW.plusSeconds(900)).id(UUID.randomUUID().toString())
                .claim(JwtTokenService.CREDENTIAL_VERSION_CLAIM, 0L);
    }

    private JwtClaimsSet.Builder baseClaimsWithoutCredentialVersion() {
        return JwtClaimsSet.builder().issuer("issuer-test").audience(List.of("audience-test"))
                .issuedAt(NOW).notBefore(NOW).expiresAt(NOW.plusSeconds(900)).subject("7")
                .id(UUID.randomUUID().toString());
    }

    private JwtClaimsSet.Builder baseClaimsWithoutIssuedAt() {
        return JwtClaimsSet.builder().issuer("issuer-test").audience(List.of("audience-test"))
                .notBefore(NOW).expiresAt(NOW.plusSeconds(900)).subject("7").id(UUID.randomUUID().toString())
                .claim(JwtTokenService.CREDENTIAL_VERSION_CLAIM, 0L);
    }

    private JwtClaimsSet.Builder baseClaimsWithoutNotBefore() {
        return JwtClaimsSet.builder().issuer("issuer-test").audience(List.of("audience-test"))
                .issuedAt(NOW).expiresAt(NOW.plusSeconds(900)).subject("7").id(UUID.randomUUID().toString())
                .claim(JwtTokenService.CREDENTIAL_VERSION_CLAIM, 0L);
    }

    private JwtClaimsSet.Builder baseClaimsWithoutId() {
        return JwtClaimsSet.builder().issuer("issuer-test").audience(List.of("audience-test"))
                .issuedAt(NOW).notBefore(NOW).expiresAt(NOW.plusSeconds(900)).subject("7")
                .claim(JwtTokenService.CREDENTIAL_VERSION_CLAIM, 0L);
    }

    private void assertDecodingRejected(String token) {
        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    private void assertValidationRejected(String token) {
        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtValidationException.class);
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
