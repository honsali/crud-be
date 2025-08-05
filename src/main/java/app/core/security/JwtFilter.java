package app.core.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.GenericFilterBean;
import app.core.auth.AuthResult;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class JwtFilter extends GenericFilterBean {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtParser parser;

    public JwtFilter(SecurityProperties securityProperties) {
        SecretKey signingKey = Keys.hmacShaKeyFor(securityProperties.getJwtBase64Secret().getBytes(StandardCharsets.UTF_8));
        this.parser = Jwts.parserBuilder().setSigningKey(signingKey).build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authorization = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNotBlank(authorization) && authorization.startsWith(AuthResult.TOKEN_PREFIX)) {
            String token = authorization.substring(AuthResult.TOKEN_PREFIX.length());
            try {
                Claims claims = parser.parseClaimsJws(token).getBody();
                List<SimpleGrantedAuthority> authorities = readAuthorities(claims);
                User principal = new User(claims.getSubject(), "", authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(principal, token, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException e) {
                log.info("Invalid JWT token");
                log.trace("Invalid JWT token: {}", e.getMessage(), e);

            }
        }
        chain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> readAuthorities(Claims claims) {
        Object tokenauthorities = claims.get("auth");

        if (tokenauthorities == null) {
            return List.of();
        }

        return Stream.of(tokenauthorities.toString().split(",")).filter(StringUtils::isNotBlank).map(SimpleGrantedAuthority::new).toList();
    }

}
