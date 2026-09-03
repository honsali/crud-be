package app.domain.admin.role;

import java.util.Locale;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import app.core.persistence.BaseEntity;

@Entity
@Table(name = "app_role")
public class Role extends BaseEntity {

    private String libelle;

    protected Role() {}

    Role(String libelle) {
        this.libelle = normalizeCode(libelle);
    }

    public String getLibelle() {
        return libelle;
    }

    public String getAuthority() {
        return "ROLE_" + libelle;
    }

    public static String normalizeCode(String value) {
        if (value == null) {
            return null;
        }
        String code = value.strip().toUpperCase(Locale.ROOT);
        return code.startsWith("ROLE_") ? code.substring("ROLE_".length()) : code;
    }
}
