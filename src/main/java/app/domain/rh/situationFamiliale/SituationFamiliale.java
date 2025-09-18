package app.domain.rh.situationFamiliale;

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
@Table(name = "situation_familiale")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SituationFamiliale implements Serializable {

    private static final long serialVersionUID = 764703712L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_situation_familiale")
    @SequenceGenerator(name = "seq_situation_familiale", allocationSize = 1)
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

    public SituationFamiliale id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdSituationFamiliale() {
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
        if (!(o instanceof SituationFamiliale)) {
            return false;
        }
        return getId() != null && getId().equals(((SituationFamiliale) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}