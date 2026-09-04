package app.domain.admin.role;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import app.core.persistence.BaseEntity;

@Entity
@Table(name = "app_role")
public class Role extends BaseEntity {

    private String libelle;

    protected Role() {}

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
