package app;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import app.core.exception.ResourceNotFoundException;
import app.core.security.jwt.LiveAccountAuthenticationToken;
import app.domain.admin.account.AccountController;
import app.domain.admin.account.AccountCreateRequest;
import app.domain.admin.account.AccountResponse;
import app.domain.admin.account.AccountSearchCriteria;
import app.domain.admin.account.AccountService;
import app.domain.admin.role.RoleController;
import app.domain.admin.role.RoleCreateRequest;
import app.domain.admin.role.RoleReference;
import app.domain.admin.role.RoleResponse;
import app.domain.admin.role.RoleService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {RoleController.class, AccountController.class})
@WithMockUser(roles = "ADMIN")
class AdminHttpContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private AccountService accountService;

    @Test
    void createsARoleWithLocationCanonicalInputAndJavascriptSafeId() throws Exception {
        long id = 9_007_199_254_740_993L;
        when(roleService.create(any(RoleCreateRequest.class)))
                .thenReturn(new RoleResponse(id, "ADMIN", "Administrateur", null, 0));

        mockMvc.perform(post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":" admin ","libelle":" Administrateur "}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/roles/" + id))
                .andExpect(jsonPath("$.id").value("9007199254740993"))
                .andExpect(jsonPath("$.code").value("ADMIN"))
                .andExpect(jsonPath("$.version").value(0));

        ArgumentCaptor<RoleCreateRequest> captor = ArgumentCaptor.forClass(RoleCreateRequest.class);
        verify(roleService).create(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().code()).isEqualTo("ADMIN");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().libelle()).isEqualTo("Administrateur");
    }

    @Test
    void listsRolesInTheOrderProvidedByTheUseCase() throws Exception {
        when(roleService.list()).thenReturn(List.of(
                new RoleResponse(2L, "ADMIN", "Administrateur", null, 0),
                new RoleResponse(1L, "USER", "Utilisateur", null, 0)));

        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[0].code").value("ADMIN"))
                .andExpect(jsonPath("$[1].id").value("1"))
                .andExpect(jsonPath("$[1].code").value("USER"));
    }

    @Test
    void validatesRoleCodeAndRequiredFields() throws Exception {
        mockMvc.perform(post("/api/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"1 bad","libelle":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[*].field", hasItems("code", "libelle")));

        verify(roleService, never()).create(any());
    }

    @Test
    void createsAnAccountFromAStringRoleIdAndEmbedsTheRoleReference() throws Exception {
        long roleId = 9_007_199_254_740_993L;
        AccountResponse response = new AccountResponse(
                42L,
                "alice.admin",
                "Alice Admin",
                "alice@example.test",
                true,
                new RoleReference(roleId, "ADMIN", "Administrateur"),
                0);
        when(accountService.create(any(AccountCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username":" Alice.Admin ",
                                  "displayName":" Alice Admin ",
                                  "email":" Alice@Example.Test ",
                                  "active":true,
                                  "roleId":"9007199254740993"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/accounts/42"))
                .andExpect(jsonPath("$.id").value("42"))
                .andExpect(jsonPath("$.username").value("alice.admin"))
                .andExpect(jsonPath("$.role.id").value("9007199254740993"))
                .andExpect(jsonPath("$.role.code").value("ADMIN"))
                .andExpect(jsonPath("$.version").value(0));

        ArgumentCaptor<AccountCreateRequest> captor = ArgumentCaptor.forClass(AccountCreateRequest.class);
        verify(accountService).create(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().roleId()).isEqualTo(roleId);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().username()).isEqualTo("alice.admin");
        org.assertj.core.api.Assertions.assertThat(captor.getValue().email()).isEqualTo("alice@example.test");
    }

    @Test
    void exposesAnApplicationPageAndMapsEveryAccountSearchCriterion() throws Exception {
        AccountResponse account = new AccountResponse(
                7L,
                "alice.admin",
                "Alice Admin",
                "alice@example.test",
                true,
                new RoleReference(3L, "ADMIN", "Administrateur"),
                2);
        when(accountService.search(any(AccountSearchCriteria.class)))
                .thenReturn(new PageImpl<>(List.of(account), PageRequest.of(1, 1), 3));

        mockMvc.perform(get("/api/admin/accounts")
                        .param("username", "alice")
                        .param("displayName", "Admin")
                        .param("email", "example")
                        .param("active", "true")
                        .param("roleId", "3")
                        .param("page", "1")
                        .param("size", "1")
                        .param("sort", "role")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("7"))
                .andExpect(jsonPath("$.items[0].role.id").value("3"))
                .andExpect(jsonPath("$.items[0].version").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(3));

        ArgumentCaptor<AccountSearchCriteria> captor = ArgumentCaptor.forClass(AccountSearchCriteria.class);
        verify(accountService).search(captor.capture());
        AccountSearchCriteria criteria = captor.getValue();
        org.assertj.core.api.Assertions.assertThat(criteria.username()).isEqualTo("alice");
        org.assertj.core.api.Assertions.assertThat(criteria.displayName()).isEqualTo("Admin");
        org.assertj.core.api.Assertions.assertThat(criteria.email()).isEqualTo("example");
        org.assertj.core.api.Assertions.assertThat(criteria.active()).isTrue();
        org.assertj.core.api.Assertions.assertThat(criteria.roleId()).isEqualTo(3L);
        org.assertj.core.api.Assertions.assertThat(criteria.page()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(criteria.size()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(criteria.sort().name()).isEqualTo("ROLE");
        org.assertj.core.api.Assertions.assertThat(criteria.direction().name()).isEqualTo("DESC");
    }

    @Test
    void rejectsAnAccountTextFilterLongerThan250Characters() throws Exception {
        mockMvc.perform(get("/api/admin/accounts").param("email", "x".repeat(251)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verify(accountService, never()).search(any());
    }

    @Test
    void deletesAnAccountWithNoContent() throws Exception {
        LiveAccountAuthenticationToken authentication = mock(LiveAccountAuthenticationToken.class);
        when(authentication.getAccountId()).thenReturn(3L);

        mockMvc.perform(delete("/api/admin/accounts/8").principal(authentication))
                .andExpect(status().isNoContent());

        verify(accountService).delete(8L, 3L);
    }

    @Test
    void mapsMissingAdminResourcesToNotFound() throws Exception {
        when(roleService.get(99L)).thenThrow(new ResourceNotFoundException("Rôle", 99L));

        mockMvc.perform(get("/api/admin/roles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/admin/roles/99"));
    }

    @Test
    void refusesDeletingAReferencedRoleWithTheConflictContract() throws Exception {
        doThrow(new DataIntegrityViolationException("fk_account_role"))
                .when(roleService).delete(4L);

        mockMvc.perform(delete("/api/admin/roles/4"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_VIOLATION"))
                .andExpect(jsonPath("$.message").value("L'opération viole une contrainte d'intégrité"));
    }
}
