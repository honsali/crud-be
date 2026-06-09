package app.domain.rh.conge;

import java.time.LocalDate;
import app.domain.rh.employe.Employe;
import app.domain.rh.typeConge.TypeConge;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "conge")
public class Conge {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_conge")
    @SequenceGenerator(name = "seq_conge", sequenceName = "seq_conge", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "code")
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    private TypeConge typeConge;

    @Column(name = "date_debut_conge")
    private LocalDate dateDebutConge;

    @Column(name = "date_fin_conge")
    private LocalDate dateFinConge;

    @Column(name = "commentaire")
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    private Employe employe;

    public Long getId() {
        return id;
    }

    public Long getIdConge() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public TypeConge getTypeConge() {
        return typeConge;
    }

    public void setTypeConge(TypeConge typeConge) {
        this.typeConge = typeConge;
    }

    public LocalDate getDateDebutConge() {
        return dateDebutConge;
    }

    public void setDateDebutConge(LocalDate dateDebutConge) {
        this.dateDebutConge = dateDebutConge;
    }

    public LocalDate getDateFinConge() {
        return dateFinConge;
    }

    public void setDateFinConge(LocalDate dateFinConge) {
        this.dateFinConge = dateFinConge;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Conge other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
