package app.core.security.web;

import app.core.security.authentication.AuthenticatedAccountPrincipal;
import app.core.security.authentication.InvalidCredentialsException;
import app.core.security.jwt.JwtAccessTokenResponse;
import app.core.security.jwt.JwtTokenService;
import app.core.security.ratelimit.LoginAttemptLimiter;
import app.core.security.ratelimit.TooManyLoginAttemptsException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokenService;
    private final LoginAttemptLimiter limiter;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenService tokenService,
            LoginAttemptLimiter limiter) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.limiter = limiter;
    }

    public JwtAccessTokenResponse login(LoginRequest request, String sourceAddress) {
        String username = limiter.canonicalUsername(request.username());
        if (!limiter.tryAcquire(username, sourceAddress)) {
            throw new TooManyLoginAttemptsException();
        }
        try {
            Authentication result = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, request.password()));
            limiter.recordSuccess(username);
            return tokenService.issue((AuthenticatedAccountPrincipal) result.getPrincipal());
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }
    }
}
