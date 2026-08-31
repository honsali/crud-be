package app.core.security.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import app.core.security.authentication.InvalidCredentialsException;
import app.core.security.config.SecurityProperties;
import app.core.security.jwt.JwtTokenService;
import app.core.security.ratelimit.LoginAttemptLimiter;
import app.core.security.ratelimit.TooManyLoginAttemptsException;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

class AuthServiceConcurrencyTest {

    @Test
    void onlyAtomicallyAdmittedRequestsCanReachTheExpensiveAuthentication() throws Exception {
        int threshold = 3;
        int requestCount = 16;
        SecurityProperties properties = new SecurityProperties();
        properties.getLoginRateLimit().setUsernameMaxAttempts(threshold);
        properties.getLoginRateLimit().setUsernameWindow(Duration.ofMinutes(5));
        properties.getLoginRateLimit().setAddressMaxAttempts(100);
        properties.getLoginRateLimit().setAddressWindow(Duration.ofMinutes(5));
        properties.getLoginRateLimit().setMaximumSize(100);
        LoginAttemptLimiter limiter = new LoginAttemptLimiter(
                properties,
                Clock.fixed(Instant.parse("2026-08-10T12:00:00Z"), ZoneOffset.UTC));
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        JwtTokenService tokenService = mock(JwtTokenService.class);
        CountDownLatch expensiveAuthenticationEntered = new CountDownLatch(threshold);
        CountDownLatch releaseExpensiveAuthentication = new CountDownLatch(1);
        AtomicInteger authenticationCalls = new AtomicInteger();
        when(authenticationManager.authenticate(any())).thenAnswer(ignored -> {
            authenticationCalls.incrementAndGet();
            expensiveAuthenticationEntered.countDown();
            if (!releaseExpensiveAuthentication.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("L'authentification coûteuse n'a pas été libérée");
            }
            throw new BadCredentialsException("échec attendu");
        });
        AuthService service = new AuthService(authenticationManager, tokenService, limiter);

        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch rejectedBeforeArgon = new CountDownLatch(requestCount - threshold);
        ExecutorService executor = Executors.newFixedThreadPool(requestCount);
        List<Future<Class<? extends RuntimeException>>> results = new ArrayList<>();
        try {
            for (int index = 0; index < requestCount; index++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Le départ concurrent n'a pas été libéré");
                    }
                    try {
                        service.login(new LoginRequest(" Same.User ", "wrong-password"), "10.0.0.42");
                        throw new AssertionError("Le login ne devait pas réussir");
                    } catch (TooManyLoginAttemptsException exception) {
                        rejectedBeforeArgon.countDown();
                        return TooManyLoginAttemptsException.class;
                    } catch (InvalidCredentialsException exception) {
                        return InvalidCredentialsException.class;
                    }
                }));
            }

            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(expensiveAuthenticationEntered.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(rejectedBeforeArgon.await(5, TimeUnit.SECONDS))
                    .as("les refus doivent finir pendant que les admissions sont encore dans l'authentification")
                    .isTrue();
            assertThat(authenticationCalls).hasValue(threshold);

            releaseExpensiveAuthentication.countDown();
            List<Class<? extends RuntimeException>> exceptionTypes = new ArrayList<>();
            for (Future<Class<? extends RuntimeException>> result : results) {
                exceptionTypes.add(result.get(5, TimeUnit.SECONDS));
            }
            assertThat(exceptionTypes).filteredOn(InvalidCredentialsException.class::equals).hasSize(threshold);
            assertThat(exceptionTypes).filteredOn(TooManyLoginAttemptsException.class::equals)
                    .hasSize(requestCount - threshold);
            verify(tokenService, never()).issue(any());
        } finally {
            start.countDown();
            releaseExpensiveAuthentication.countDown();
            executor.shutdownNow();
        }
    }
}
