package app.core.security;

import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
class AuthResource {

    private static final String ISSUER = "app_core";

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final long tokenValiditySeconds;

    AuthResource(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder, @Value("${application.security.token-validity-seconds}") long tokenValiditySeconds) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    @PostMapping("/api/authenticate")
    ResponseEntity<TokenResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        Instant now = Instant.now();
        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(tokenValiditySeconds))
                .subject(authentication.getName())
                .claim(SecurityConfiguration.AUTHORITIES_CLAIM, authorities)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(SecurityConfiguration.JWT_ALGORITHM).build(), claims)).getTokenValue();

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new TokenResponse(token));
    }

    record LoginRequest(@NotBlank String username, @NotBlank String password, Boolean rememberMe) {
    }

    record TokenResponse(@JsonProperty("id_token") String idToken) {
    }
}
