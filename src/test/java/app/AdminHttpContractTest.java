package app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import app.domain.admin.account.AccountController;
import app.domain.admin.account.AccountCreateRequest;
import app.domain.admin.account.AccountResponse;
import app.domain.admin.account.AccountService;
import app.domain.admin.account.AccountUpdateRequest;
import app.domain.admin.account.PasswordResetRequest;
import app.domain.admin.role.Role;
import app.domain.admin.role.RoleReference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
@WithMockUser(roles = "ADMIN")
class AdminHttpContractTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AccountService accountService;

    @Test
    void exposesTheFiveAccountOperationsUsedByTheFrontend() throws Exception {
        AccountResponse account = account(9_007_199_254_740_993L, "alice", "ROLE_ADMIN", true);
        when(accountService.creer(any())).thenReturn(account);
        when(accountService.lister()).thenReturn(List.of(account));
        when(accountService.recupererParId(account.id())).thenReturn(account);
        when(accountService.maj(any(), any())).thenReturn(account);

        mockMvc.perform(post("/api/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":" Alice ","password":"password-123","role":"ROLE_ADMIN"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/accounts/" + account.id()))
                .andExpect(jsonPath("$.id").value("9007199254740993"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role.code").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.role.libelle").value("ADMIN"))
                .andExpect(jsonPath("$.activated").value(true));

        mockMvc.perform(get("/api/admin/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"));
        mockMvc.perform(get("/api/admin/accounts/{id}", account.id()))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/admin/accounts/{id}", account.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ROLE_ADMIN\",\"activated\":true}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/admin/accounts/{id}/password", account.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"new-password-123\"}"))
                .andExpect(status().isNoContent());

        verify(accountService).reinitialiserMotDePasse(any(), any(PasswordResetRequest.class));
    }

    @Test
    void normalizesTheUsernameAndPublicRolePrefix() throws Exception {
        when(accountService.creer(any())).thenReturn(account(1L, "alice", "ROLE_ADMIN", true));
        ArgumentCaptor<AccountCreateRequest> captor = ArgumentCaptor.forClass(AccountCreateRequest.class);

        mockMvc.perform(post("/api/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":" Alice.Admin ","password":"password-123","role":"ROLE_ADMIN"}
                                """))
                .andExpect(status().isCreated());

        verify(accountService).creer(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().username()).isEqualTo("alice.admin");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().role()).isEqualTo("ADMIN");
    }

    @Test
    void validatesCreateUpdateAndPasswordBodies() throws Exception {
        mockMvc.perform(post("/api/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a\",\"password\":\"short\",\"role\":\"\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/admin/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/admin/accounts/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    private AccountResponse account(Long id, String username, String role, boolean activated) {
        return new AccountResponse(id, username, new RoleReference(1L, role, Role.normalizeCode(role)), activated);
    }
}
