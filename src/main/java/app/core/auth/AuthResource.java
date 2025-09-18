package app.core.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.core.security.SecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/authenticate")
public class AuthResource {

    private static final String AUTHORITIES_KEY = "auth";
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityProperties securityProperties;

    public AuthResource(SecurityProperties securityProperties, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityProperties = securityProperties;
    }

    @GetMapping
    public String getUsername(HttpServletRequest request) {
        return request.getRemoteUser();
    }

    @PostMapping
    public ResponseEntity<AuthResult> authorize(@Valid @RequestBody AuthQuery authenticationQuery) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(authenticationQuery.getUsername(), authenticationQuery.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        Duration tokenValidity = authenticationQuery.isRememberMe() ? this.securityProperties.getRememberMeTokenValidity() : this.securityProperties.getTokenValidity();
        Date validityDate = Date.from(Instant.now().plusSeconds(tokenValidity.toSeconds()));
        SecretKey key = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(securityProperties.getJwtBase64Secret()));

        String jwtToken = Jwts.builder().setSubject(authenticationQuery.getUsername()).claim(AUTHORITIES_KEY, authorities).setExpiration(validityDate).setIssuedAt(new Date()).signWith(key).compact();

        AuthResult authResult = new AuthResult(jwtToken);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, authResult.getBearer());

        return new ResponseEntity<>(authResult, httpHeaders, HttpStatus.OK);
    }

}
