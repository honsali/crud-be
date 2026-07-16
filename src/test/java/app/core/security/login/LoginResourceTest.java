package app.core.security.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import app.core.security.account.AppRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class LoginResourceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtEncoder jwtEncoder;

    private LoginResource loginResource;

    @BeforeEach
    void setUp() {
        loginResource = new LoginResource(
                authenticationManager,
                jwtEncoder,
                new JwtProperties("issuer", "audience", 3600));
    }

    @Test
    void issuesClaimsFromTheSnapshotThatAuthenticatedThePassword() throws Exception {
        LoginPrincipal principal = new LoginPrincipal(
                7L,
                "manager",
                "hash",
                AppRole.ROLE_GESTIONNAIRE_RH,
                3L,
                true);
        assertThat(principal.toString()).doesNotContain("hash");
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtEncoder.encode(any())).thenReturn(Jwt.withTokenValue("encoded-token")
                .header("alg", "HS512")
                .subject("manager")
                .build());

        var response = loginResource.login(new LoginResource.LoginRequest(" manager ", "password"));

        ArgumentCaptor<JwtEncoderParameters> parameters = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        org.mockito.Mockito.verify(jwtEncoder).encode(parameters.capture());
        assertThat(parameters.getValue().getClaims().getClaims())
                .containsEntry(JwtToken.ACCOUNT_ID_CLAIM, 7L)
                .containsEntry(JwtToken.TOKEN_VERSION_CLAIM, 3L)
                .containsEntry(JwtToken.ROLE_CLAIM, AppRole.ROLE_GESTIONNAIRE_RH.name());
        assertThat(response).isEqualTo(new LoginResource.LoginResponse("encoded-token"));
        assertThat(JsonMapper.builder().build().writeValueAsString(response))
                .isEqualTo("{\"accessToken\":\"encoded-token\"}");
    }
}
