package app.domain.rh.conge;

import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
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
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Conge implements Serializable {

    private static final long serialVersionUID = 334770216L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_conge")
    @SequenceGenerator(name = "seq_conge", allocationSize = 1)
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
        return this.id;
    }

    public Conge id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayString() {
        return code;
    }

    public Long getIdConge() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public TypeConge getTypeConge() {
        return this.typeConge;
    }

    public void setTypeConge(TypeConge typeConge) {
        this.typeConge = typeConge;
    }

    public LocalDate getDateDebutConge() {
        return this.dateDebutConge;
    }

    public void setDateDebutConge(LocalDate dateDebutConge) {
        this.dateDebutConge = dateDebutConge;
    }

    public LocalDate getDateFinConge() {
        return this.dateFinConge;
    }

    public void setDateFinConge(LocalDate dateFinConge) {
        this.dateFinConge = dateFinConge;
    }

    public String getCommentaire() {
        return this.commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Employe getEmploye() {
        return this.employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Conge)) {
            return false;
        }
        return getId() != null && getId().equals(((Conge) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}