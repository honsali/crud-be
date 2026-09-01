package app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import app.core.exception.ApiExceptionHandler;
import app.core.exception.ConflictException;
import app.core.security.account.SecurityAccountProvider;
import app.core.security.account.SecurityAccountSnapshot;
import app.core.security.config.AuthenticationConfiguration;
import app.core.security.config.HttpSecurityConfiguration;
import app.core.security.config.JwtConfiguration;
import app.core.security.config.SecurityBeansConfiguration;
import app.core.security.config.SecurityProperties;
import app.core.security.credential.CredentialService;
import app.core.security.credential.InvalidCurrentPasswordException;
import app.core.security.credential.LoginCredentialSnapshot;
import app.core.security.jwt.JwtTokenService;
import app.core.security.ratelimit.LoginAttemptLimiter;
import app.core.security.web.AdminCredentialController;
import app.core.security.web.AuthController;
import app.core.security.web.AuthService;
import app.core.security.web.JsonAccessDeniedHandler;
import app.core.security.web.JsonAuthenticationEntryPoint;
import app.domain.admin.account.AccountController;
import app.domain.admin.account.AccountResponse;
import app.domain.admin.account.AccountService;
import app.domain.admin.role.RoleReference;
import app.domain.rh.departement.DepartementController;
import app.domain.rh.departement.DepartementResponse;
import app.domain.rh.departement.DepartementService;
import jakarta.servlet.http.Cookie;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = {
                AuthController.class,
                AdminCredentialController.class,
                DepartementController.class,
                AccountController.class
})
@Import({
                SecurityBeansConfiguration.class,
                JwtConfiguration.class,
                AuthenticationConfiguration.class,
                HttpSecurityConfiguration.class,
                JwtTokenService.class,
                AuthService.class,
                LoginAttemptLimiter.class,
                JsonAuthenticationEntryPoint.class,
                JsonAccessDeniedHandler.class,
                ApiExceptionHandler.class,
                SecurityHttpTest.FixedClockConfiguration.class
})
@ImportAutoConfiguration({
                SecurityAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
})
@TestPropertySource(properties = {
                "app.security.cors.allowed-origins=https://allowed.example",
                "app.security.login-rate-limit.username-max-attempts=4",
                "app.security.login-rate-limit.address-max-attempts=100"
})
class SecurityHttpTest {

        @TestConfiguration
        static class FixedClockConfiguration {
                @Bean
                @Primary
                Clock fixedClock() {
                        return Clock.fixed(Instant.parse("2026-08-10T12:00:00Z"), ZoneOffset.UTC);
                }
        }
        private record LoginBody(String username, String password) {
        }

        private record PasswordChangeBody(String currentPassword, String newPassword) {
        }
        private record AccountUpdateBody(
                        String username,
                        String displayName,
                        String email,
                        boolean active,
                        Long roleId,
                        Long version) {
        }

        private static final Long ACCOUNT_ID = 7L;
        private static final String PASSWORD = " mot de passe très sûr ";
        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;
        @Autowired
        PasswordEncoder passwordEncoder;
        @Autowired
        JwtEncoder jwtEncoder;
        @Autowired
        SecurityProperties securityProperties;

        @MockitoBean
        SecurityAccountProvider accountProvider;
        @MockitoBean
        CredentialService credentialService;
        @MockitoBean
        DepartementService departementService;

        @MockitoBean
        AccountService accountService;

        private final AtomicReference<SecurityAccountSnapshot> liveAccount = new AtomicReference<>();

        private final AtomicLong liveCredentialVersion = new AtomicLong();

        private String passwordHash;

        @BeforeEach
        void configureLiveState() {
                liveAccount.set(new SecurityAccountSnapshot(ACCOUNT_ID, "alice.admin", true, "ADMIN"));
                liveCredentialVersion.set(0);
                passwordHash = passwordEncoder.encode(PASSWORD);
                when(accountProvider.findByUsername(any())).thenAnswer(invocation -> {
                        SecurityAccountSnapshot account = liveAccount.get();
                        String requested = invocation.getArgument(0);
                        return account != null && account.username().equals(requested) ? Optional.of(account) : Optional.empty();
                });
                when(accountProvider.findById(ACCOUNT_ID)).thenAnswer(ignored -> Optional.ofNullable(liveAccount.get()));
                when(credentialService.findForLogin(ACCOUNT_ID)).thenAnswer(ignored -> Optional.of(new LoginCredentialSnapshot(ACCOUNT_ID, passwordHash, liveCredentialVersion.get())));
                when(credentialService.findTokenVersion(ACCOUNT_ID)).thenAnswer(ignored -> Optional.of(liveCredentialVersion.get()));
                when(departementService.lister()).thenReturn(List.of(new DepartementResponse(1L, "Support", null, 0)));
                when(accountService.get(ACCOUNT_ID)).thenReturn(new AccountResponse(
                                ACCOUNT_ID, "alice.admin", "Alice", null, true,
                                new RoleReference(1L, "ADMIN", "Administrateur"), 0));
                doAnswer(ignored -> {
                        liveCredentialVersion.incrementAndGet();
                        return null;
                })
                                .when(credentialService).changePassword(eq(ACCOUNT_ID), any(), any());
                doAnswer(ignored -> {
                        liveCredentialVersion.incrementAndGet();
                        return null;
                })
                                .when(credentialService).resetPassword(eq(ACCOUNT_ID), any());
                doAnswer(ignored -> {
                        liveCredentialVersion.incrementAndGet();
                        return null;
                })
                                .when(credentialService).revokeAllTokens(ACCOUNT_ID);
        }

        @Test
        void loginCanonicalizesUsernameReturnsNoStoreTokenAndCreatesNoSession() throws Exception {
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"username":" Alice.Admin ","password":" mot de passe très sûr "}
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                                .andExpect(header().string("Pragma", "no-cache"))
                                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                                .andExpect(jsonPath("$.expiresIn").value(900))
                                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                                .andExpect(cookie().doesNotExist("JSESSIONID"));
        }

        @Test
        void allInvalidLoginStatesReturnTheSameGenericContract() throws Exception {
                MvcResult wrongPassword = invalidLogin("alice.admin", "wrong-password-value");
                liveAccount.set(null);
                MvcResult unknown = invalidLogin("unknown.user", PASSWORD);
                liveAccount.set(new SecurityAccountSnapshot(ACCOUNT_ID, "alice.admin", false, "ADMIN"));
                MvcResult inactive = invalidLogin("alice.admin", PASSWORD);
                liveAccount.set(new SecurityAccountSnapshot(ACCOUNT_ID, "alice.admin", true, "ADMIN"));
                when(credentialService.findForLogin(ACCOUNT_ID)).thenReturn(Optional.empty());
                MvcResult missingCredential = invalidLogin("alice.admin", PASSWORD);

                for (MvcResult result : List.of(wrongPassword, unknown, inactive, missingCredential)) {
                        JsonNode error = objectMapper.readTree(result.getResponse().getContentAsString());
                        assertThat(result.getResponse().getStatus()).isEqualTo(401);
                        assertThat(error.get("code").asText()).isEqualTo("INVALID_CREDENTIALS");
                        assertThat(error.get("message").asText()).isEqualTo("Identifiants invalides");
                        assertThat(result.getResponse().getContentAsString())
                                        .doesNotContain("password").doesNotContain("argon2").doesNotContain("accessToken");
                }
        }

        @Test
        void rateLimitReturnsAGeneric429AfterTheConfiguredThreshold() throws Exception {
                for (int attempt = 0; attempt < 4; attempt++) {
                        invalidLogin("blocked.user", "wrong-password-value");
                }
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                new LoginBody(" BLOCKED.USER ", "wrong-password-value"))))
                                .andExpect(status().isTooManyRequests())
                                .andExpect(jsonPath("$.code").value("TOO_MANY_LOGIN_ATTEMPTS"))
                                .andExpect(jsonPath("$.message").value("Trop de tentatives de connexion"));
        }

        @Test
        void protectsEndpointsAndUsesTheLiveRoleForAuthorization() throws Exception {
                mockMvc.perform(get("/api/rh/departements"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

                String token = loginToken();
                mockMvc.perform(get("/api/rh/departements").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value("1"));

                liveAccount.set(new SecurityAccountSnapshot(ACCOUNT_ID, "alice.admin", true, "USER"));
                mockMvc.perform(get("/api/admin/accounts/7").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

                liveAccount.set(new SecurityAccountSnapshot(ACCOUNT_ID, "alice.admin", true, "ADMIN"));
                mockMvc.perform(get("/api/admin/accounts/7").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                                .andExpect(status().isOk());
        }

        @Test
        void rejectsBearerInQueryOrCookie() throws Exception {
                String token = loginToken();
                mockMvc.perform(get("/api/rh/departements").queryParam("access_token", token))
                                .andExpect(status().isUnauthorized());
                mockMvc.perform(get("/api/rh/departements").cookie(new Cookie("access_token", token)))
                                .andExpect(status().isUnauthorized());

                char replacement = token.endsWith("A") ? 'Q' : 'A';
                String altered = token.substring(0, token.length() - 1) + replacement;
                mockMvc.perform(get("/api/rh/departements").header(HttpHeaders.AUTHORIZATION, bearer(altered)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        }

        @Test
        void signedJwtWithoutAudienceReturnsTheExactGeneric401Contract() throws Exception {
                Instant now = Instant.parse("2026-08-10T12:00:00Z");
                JwtClaimsSet claims = JwtClaimsSet.builder()
                                .issuer(securityProperties.getJwt().getIssuer())
                                .issuedAt(now)
                                .notBefore(now)
                                .expiresAt(now.plusSeconds(900))
                                .subject(ACCOUNT_ID.toString())
                                .id(UUID.randomUUID().toString())
                                .claim(JwtTokenService.CREDENTIAL_VERSION_CLAIM, liveCredentialVersion.get())
                                .build();
                String tokenWithoutAudience = jwtEncoder.encode(JwtEncoderParameters.from(
                                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims)).getTokenValue();

                mockMvc.perform(get("/api/auth/me")
                                .header(HttpHeaders.AUTHORIZATION, bearer(tokenWithoutAudience)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"))
                                .andExpect(jsonPath("$.message").value("Une authentification est requise"))
                                .andExpect(jsonPath("$.path").value("/api/auth/me"))
                                .andExpect(jsonPath("$.fieldErrors").isEmpty())
                                .andExpect(content().string(not(containsString("NullPointerException"))))
                                .andExpect(content().string(not(containsString("JwtValidationException"))))
                                .andExpect(content().string(not(containsString("invalid_token"))));
        }

        @Test
        void corsAllowsOnlyConfiguredExactOriginAndHandlesPreflight() throws Exception {
                mockMvc.perform(options("/api/rh/departements")
                                .header(HttpHeaders.ORIGIN, "https://allowed.example")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                                .andExpect(status().isOk())
                                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://allowed.example"));

                mockMvc.perform(options("/api/rh/departements")
                                .header(HttpHeaders.ORIGIN, "https://evil.example")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                                .andExpect(status().isForbidden())
                                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }

        @Test
        void corsAllowsAnAuthenticatedDepartementCreationWithoutLocation() throws Exception {
                String token = loginToken();
                when(departementService.creer(any())).thenReturn(new DepartementResponse(42L, "Finance", null, 0));

                mockMvc.perform(post("/api/rh/departements")
                                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                                .header(HttpHeaders.ORIGIN, "https://allowed.example")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"nom\":\"Finance\"}"))
                                .andExpect(status().isCreated())
                                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://allowed.example"))
                                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Location"))
                                .andExpect(header().doesNotExist(HttpHeaders.LOCATION));
        }

        @Test
        void wrongCurrentPasswordIsBadRequestWhileLoginFailureRemainsUnauthorized() throws Exception {
                String token = loginToken();
                String wrongCurrentPassword = "mauvais mot de passe actuel";
                doThrow(new InvalidCurrentPasswordException()).when(credentialService)
                                .changePassword(ACCOUNT_ID, wrongCurrentPassword, "nouveau mot de passe très sûr");

                mockMvc.perform(put("/api/auth/password")
                                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new PasswordChangeBody(
                                                wrongCurrentPassword, "nouveau mot de passe très sûr"))))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("INVALID_CURRENT_PASSWORD"))
                                .andExpect(jsonPath("$.message").value("Le mot de passe actuel est incorrect"))
                                .andExpect(content().string(not(containsString(wrongCurrentPassword))))
                                .andExpect(content().string(not(containsString("argon2"))));

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                new LoginBody("alice.admin", "autre mauvais mot de passe"))))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }

        @Test
        void authenticatedAdminCannotLockItsOwnAccountButCanEditItsProfile() throws Exception {
                String token = loginToken();
                doThrow(new ConflictException("Un administrateur ne peut pas supprimer son propre compte"))
                                .when(accountService).delete(ACCOUNT_ID, ACCOUNT_ID);
                when(accountService.update(eq(ACCOUNT_ID), argThat(request -> !request.active()), eq(ACCOUNT_ID)))
                                .thenThrow(new ConflictException("Un administrateur ne peut pas désactiver son propre compte"));
                when(accountService.update(
                                eq(ACCOUNT_ID),
                                argThat(request -> request.active() && request.roleId().equals(2L)),
                                eq(ACCOUNT_ID)))
                                                .thenThrow(new ConflictException("Un administrateur ne peut pas modifier son propre rôle"));
                AccountResponse updated = new AccountResponse(
                                ACCOUNT_ID, "alice.renamed", "Alice renommée", "Alice@Example.test", true,
                                new RoleReference(1L, "ADMIN", "Administrateur"), 1);
                when(accountService.update(
                                eq(ACCOUNT_ID),
                                argThat(request -> request.active() && request.roleId().equals(1L)),
                                eq(ACCOUNT_ID)))
                                                .thenReturn(updated);

                mockMvc.perform(delete("/api/admin/accounts/7").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("CONFLICT"))
                                .andExpect(jsonPath("$.message")
                                                .value("Un administrateur ne peut pas supprimer son propre compte"));
                mockMvc.perform(put("/api/admin/accounts/7")
                                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(accountUpdateJson(false, 1L)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message")
                                                .value("Un administrateur ne peut pas désactiver son propre compte"));
                mockMvc.perform(put("/api/admin/accounts/7")
                                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(accountUpdateJson(true, 2L)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message")
                                                .value("Un administrateur ne peut pas modifier son propre rôle"));
                mockMvc.perform(put("/api/admin/accounts/7")
                                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(accountUpdateJson(true, 1L)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value("alice.renamed"));
        }

        @Test
        void revalidatesDeletionDeactivationRoleAndUsernameWithTheSameJwt() throws Exception {
                String token = loginToken();
                liveAccount.set(new SecurityAccountSnapshot(ACCOUNT_ID, "renamed.user", true, "ADMIN"));
                mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accountId").value("7"))
                                .andExpect(jsonPath("$.username").value("renamed.user"));

                liveAccount.set(new SecurityAccountSnapshot(ACCOUNT_ID, "renamed.user", false, "ADMIN"));
                assertAuthenticationRequired(token);
                liveAccount.set(null);
                assertAuthenticationRequired(token);
        }

        @Test
        void passwordChangeAdminResetAndLogoutAllImmediatelyRevokeOldJwt() throws Exception {
                String passwordToken = loginToken();
                mockMvc.perform(put("/api/auth/password")
                                .header(HttpHeaders.AUTHORIZATION, bearer(passwordToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"currentPassword":" mot de passe très sûr ","newPassword":" nouveau mot de passe très sûr "}
                                                """))
                                .andExpect(status().isNoContent());
                assertAuthenticationRequired(passwordToken);

                String resetToken = loginToken();
                mockMvc.perform(put("/api/admin/accounts/7/password")
                                .header(HttpHeaders.AUTHORIZATION, bearer(resetToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"newPassword\":\" mot de passe réinitialisé sûr \"}"))
                                .andExpect(status().isNoContent());
                assertAuthenticationRequired(resetToken);

                String logoutToken = loginToken();
                mockMvc.perform(post("/api/auth/logout-all")
                                .header(HttpHeaders.AUTHORIZATION, bearer(logoutToken)))
                                .andExpect(status().isNoContent());
                assertAuthenticationRequired(logoutToken);
        }

        private MvcResult invalidLogin(String username, String password) throws Exception {
                return mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new LoginBody(username, password))))
                                .andReturn();
        }

        private String loginToken() throws Exception {
                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new LoginBody(" Alice.Admin ", PASSWORD))))
                                .andExpect(status().isOk())
                                .andReturn();
                return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
        }

        private void assertAuthenticationRequired(String token) throws Exception {
                mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        }

        private String bearer(String token) {
                return "Bearer " + token;
        }

        private String accountUpdateJson(boolean active, Long roleId) throws Exception {
                return objectMapper.writeValueAsString(new AccountUpdateBody(
                                "alice.renamed", "Alice renommée", "Alice@Example.test", active, roleId, 0L));
        }
}
