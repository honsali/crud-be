package app.core.security.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import app.domain.admin.account.AppRole;
import tools.jackson.databind.json.JsonMapper;

class AppRoleJsonTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void acceptsAndWritesOnlyCanonicalRoleStrings() throws Exception {
        assertThat(jsonMapper.readValue("\"ROLE_ADMIN\"", AppRole.class))
                .isEqualTo(AppRole.ROLE_ADMIN);
        assertThat(jsonMapper.writeValueAsString(AppRole.ROLE_GESTIONNAIRE_RH))
                .isEqualTo("\"ROLE_GESTIONNAIRE_RH\"");
    }

    @Test
    void rejectsNumericArrayLowercaseAndUnknownRoleValues() {
        assertRejected("0");
        assertRejected("[]");
        assertRejected("\"role_admin\"");
        assertRejected("\"ROLE_UNKNOWN\"");
    }

    private void assertRejected(String json) {
        assertThatThrownBy(() -> jsonMapper.readValue(json, AppRole.class))
                .isInstanceOf(Exception.class);
    }
}
