package app.domain.rh.sexe;

import java.io.Serializable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "sexe")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Sexe implements Serializable {

    private static final long serialVersionUID = 1119645047L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_sexe")
    @SequenceGenerator(name = "seq_sexe", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "libelle", nullable = false)
    private String libelle;

    @NotNull
    @Column(name = "code", nullable = false)
    private String code;

    public Long getId() {
        return this.id;
    }

    public Sexe id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdSexe() {
        return this.id;
    }

    public String getLibelle() {
        return this.libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sexe)) {
            return false;
        }
        return getId() != null && getId().equals(((Sexe) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}