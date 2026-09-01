package app.domain.rh.situationfamiliale;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import app.core.persistence.BaseEntity;

@Entity
@Table(name = "situation_familiale")
public class SituationFamiliale extends BaseEntity {

    private String libelle;

    protected SituationFamiliale() {}

    SituationFamiliale(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
