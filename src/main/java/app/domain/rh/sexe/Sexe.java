package app.domain.rh.sexe;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import app.core.persistence.BaseEntity;

@Entity
@Table(name = "sexe")
public class Sexe extends BaseEntity {

    private String libelle;

    protected Sexe() {}

    Sexe(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
