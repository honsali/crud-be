package app.domain.rh.conge;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import app.core.persistence.BaseEntity;
import app.domain.rh.employe.Employe;
import app.domain.rh.typeConge.TypeConge;

@Entity
@Table(name = "conge")
public class Conge extends BaseEntity {

    private String code;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_conge_id")
    private TypeConge typeConge;
    private LocalDate dateDebutConge;
    private LocalDate dateFinConge;
    private String commentaire;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    protected Conge() {}

    Conge(String code, TypeConge typeConge, LocalDate dateDebutConge, LocalDate dateFinConge, String commentaire, Employe employe) {
        this.code = code;
        this.typeConge = typeConge;
        this.dateDebutConge = dateDebutConge;
        this.dateFinConge = dateFinConge;
        this.commentaire = commentaire;
        this.employe = employe;
    }

    public String getCode() {
        return code;
    }

    public TypeConge getTypeConge() {
        return typeConge;
    }

    public LocalDate getDateDebutConge() {
        return dateDebutConge;
    }

    public LocalDate getDateFinConge() {
        return dateFinConge;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void update(String code, TypeConge typeConge, LocalDate dateDebutConge, LocalDate dateFinConge, String commentaire) {
        this.code = code;
        this.typeConge = typeConge;
        this.dateDebutConge = dateDebutConge;
        this.dateFinConge = dateFinConge;
        this.commentaire = commentaire;
    }
}
