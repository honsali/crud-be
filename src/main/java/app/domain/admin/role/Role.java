package app.domain.admin.role;

import java.util.Locale;

import app.core.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_role")
public class Role extends BaseEntity {

    @Column(updatable = false)
    private String code;

    private String libelle;

    private String description;

    protected Role() {
    }

    Role(String code, String libelle, String description) {
        this.code = normalizeCode(code);
        this.libelle = normalizeLibelle(libelle);
        this.description = description;
    }

    void update(String libelle, String description) {
        this.libelle = normalizeLibelle(libelle);
        this.description = description;
    }

    public static String normalizeCode(String value) {
        if (value == null) {
            return null;
        }
        String code = value.strip().toUpperCase(Locale.ROOT);
        return code.startsWith("ROLE_") ? code.substring("ROLE_".length()) : code;
    }

    static String normalizeLibelle(String value) {
        return value == null ? null : value.strip();
    }

    public String getCode() {
        return code;
    }

    public String getAuthority() {
        return "ROLE_" + code;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }
}
