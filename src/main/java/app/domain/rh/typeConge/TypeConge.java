package app.domain.rh.typeConge;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import app.core.persistence.BaseEntity;

@Entity
@Table(name = "type_conge")
public class TypeConge extends BaseEntity {

    private String libelle;

    protected TypeConge() {}

    TypeConge(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
