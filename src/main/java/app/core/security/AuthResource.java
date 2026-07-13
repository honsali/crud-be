package app.core.security;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import jakarta.validation.constraints.Size;

@RestController
class AuthResource {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final SecurityProperties securityProperties;

    AuthResource(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder, SecurityProperties securityProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.securityProperties = securityProperties;
    }

    @PostMapping("/api/authenticate")
    ResponseEntity<TokenResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        Instant now = Instant.now();
        List<String> authorities = SecurityConfiguration.applicationRoles(authentication.getAuthorities());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(securityProperties.issuer())
                .audience(List.of(securityProperties.audience()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(securityProperties.tokenValiditySeconds()))
                .subject(authentication.getName())
                .claim(SecurityConfiguration.AUTHORITIES_CLAIM, authorities)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(SecurityConfiguration.JWT_ALGORITHM).build(), claims)).getTokenValue();

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .body(new TokenResponse(token));
    }

    record LoginRequest(@NotBlank @Size(max = 50) String username, @NotBlank @Size(max = 256) String password) {
    }

    record TokenResponse(@JsonProperty("id_token") String idToken) {
    }
}
