package app.core.configuration;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import app.core.exception.ApiSecurityExceptionHandler;
import app.core.security.account.Account;
import app.core.security.account.AccountRepository;
import app.core.security.account.AccountResource;
import app.core.security.account.AccountService;
import app.core.security.account.AppRole;
import app.core.security.login.JwtToken;
import app.core.security.login.LoginPrincipalLoader;
import app.core.security.login.LoginTokenValidator;
import app.domain.rh.departement.DepartementResource;
import app.domain.rh.departement.DepartementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(
        controllers = { AccountResource.class, DepartementResource.class, SecurityProbeController.class },
        properties = {
            "application.security.jwt-base64-secret=dGVzdC1zZWNyZXQteHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHg=",
            "application.security.issuer=test-issuer",
            "application.security.audience=test-audience"
        })
@Import({
        SecurityConfiguration.class,
        ApiSecurityExceptionHandler.class,
        LoginTokenValidator.class,
        LoginPrincipalLoader.class
})
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
class SecurityRouteMatrixTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private DepartementService departementService;

    @BeforeEach
    void setUpAccounts() {
        when(accountRepository.findById(anyLong())).thenAnswer(invocation -> {
            long id = invocation.getArgument(0);
            if (id == 1L) {
                return Optional.of(account(1L, "admin", AppRole.ROLE_ADMIN));
            }
            if (id == 2L) {
                return Optional.of(account(2L, "manager", AppRole.ROLE_GESTIONNAIRE_RH));
            }
            return Optional.empty();
        });
        when(accountService.list()).thenReturn(List.of());
        when(departementService.lister()).thenReturn(List.of());
    }

    @Test
    void administratorCanManageAccountsButCannotReachBusinessRoutes() throws Exception {
        String token = token(1L, "admin", AppRole.ROLE_ADMIN.name());

        mockMvc.perform(authenticatedGet("/api/admin/accounts", token)).andExpect(status().isOk());
        mockMvc.perform(authenticatedGet("/api/rh/departement", token)).andExpect(status().isForbidden());
    }

    @Test
    void functionalRoleCanReachBusinessRoutesButCannotManageAccounts() throws Exception {
        String token = token(2L, "manager", AppRole.ROLE_GESTIONNAIRE_RH.name());

        mockMvc.perform(authenticatedGet("/api/rh/departement", token)).andExpect(status().isOk());
        mockMvc.perform(authenticatedGet("/api/rh/future-entity", token)).andExpect(status().isOk());
        mockMvc.perform(authenticatedGet("/api/admin/accounts", token)).andExpect(status().isForbidden());
        mockMvc.perform(authenticatedGet("/api/unassigned/probe", token)).andExpect(status().isForbidden());
    }

    @Test
    void rejectsTokensWithARoleArrayOrAnUnknownRole() throws Exception {
        String roleArray = token(2L, "manager", List.of(
                AppRole.ROLE_ADMIN.name(), AppRole.ROLE_GESTIONNAIRE_RH.name()));
        String unknownRole = token(2L, "manager", "ROLE_UNKNOWN");

        mockMvc.perform(authenticatedGet("/api/admin/accounts", roleArray)).andExpect(status().isUnauthorized());
        mockMvc.perform(authenticatedGet("/api/admin/accounts", unknownRole)).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAnExistingTokenAfterTheAccountTokenVersionChanges() throws Exception {
        String token = token(2L, "manager", AppRole.ROLE_GESTIONNAIRE_RH.name());
        Account changed = account(2L, "manager", AppRole.ROLE_GESTIONNAIRE_RH);
        changed.incrementTokenVersion();
        when(accountRepository.findById(2L)).thenReturn(Optional.of(changed));

        mockMvc.perform(authenticatedGet("/api/rh/departement", token)).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsNumericRoleValuesAsBadRequests() throws Exception {
        String token = token(1L, "admin", AppRole.ROLE_ADMIN.name());

        mockMvc.perform(post("/api/admin/accounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"manager","password":"secure-password","role":0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginRouteIsPublic() throws Exception {
        mockMvc.perform(post("/api/login")).andExpect(status().isOk());
    }

    @Test
    void businessRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/rh/departement")).andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedGet(
            String path, String token) {
        return get(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private String token(long accountId, String username, Object role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("test-issuer")
                .audience(List.of("test-audience"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .subject(username)
                .claim(JwtToken.ACCOUNT_ID_CLAIM, accountId)
                .claim(JwtToken.TOKEN_VERSION_CLAIM, 0L)
                .claim(JwtToken.ROLE_CLAIM, role)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(JwtToken.ALGORITHM).build(), claims)).getTokenValue();
    }

    private static Account account(Long id, String username, AppRole role) {
        Account account = new Account();
        account.setId(id);
        account.setUsername(username);
        account.setPasswordHash("hash");
        account.setRole(role);
        account.setActivated(true);
        return account;
    }
}

@RestController
class SecurityProbeController {

    @PostMapping("/api/login")
    String login() {
        return "ok";
    }

    @GetMapping("/api/rh/future-entity")
    String futureRhEntity() {
        return "ok";
    }

    @GetMapping("/api/unassigned/probe")
    String probe() {
        return "ok";
    }
}
