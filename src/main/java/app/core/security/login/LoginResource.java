package app.core.security.login;

import java.time.Instant;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
class LoginResource {

    record LoginRequest(@NotBlank @Size(max = 50) String username, @NotBlank @Size(max = 256) String password) {
    }
    record LoginResponse(String accessToken) {
    }

    private final AuthenticationManager authenticationManager;

    private final JwtEncoder jwtEncoder;

    private final JwtProperties jwtProperties;

    LoginResource(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/api/login")
    LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username().trim(), request.password()));
        if (!(authentication.getPrincipal() instanceof LoginPrincipal principal)) {
            throw new BadCredentialsException("Authenticated account details are unavailable.");
        }
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet//
                .builder().issuer(jwtProperties.issuer())//
                .audience(List.of(jwtProperties.audience()))//
                .issuedAt(now)//
                .expiresAt(now.plusSeconds(jwtProperties.tokenValiditySeconds()))//
                .subject(principal.username())//
                .claim(JwtToken.ACCOUNT_ID_CLAIM, principal.id())//
                .claim(JwtToken.TOKEN_VERSION_CLAIM, principal.tokenVersion())//
                .claim(JwtToken.ROLE_CLAIM, principal.role().name())//
                .build();//

        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(JwtToken.ALGORITHM).build(), claims)).getTokenValue();

        return new LoginResponse(token);
    }
}
