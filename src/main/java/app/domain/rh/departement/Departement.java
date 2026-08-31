package app.domain.rh.departement;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import app.core.persistence.BaseEntity;

@Entity
@Table(name = "departement")
public class Departement extends BaseEntity {

    private String nom;
    private String description;

    protected Departement() {}

    Departement(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    public String getNom() {
        return nom;
    }

    public String getDescription() {
        return description;
    }

    public void update(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }
}
