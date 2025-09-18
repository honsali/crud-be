package app.domain.rh.typeConge;

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
@Table(name = "type_conge")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TypeConge implements Serializable {

    private static final long serialVersionUID = 325140210L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_type_conge")
    @SequenceGenerator(name = "seq_type_conge", allocationSize = 1)
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

    public TypeConge id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdTypeConge() {
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
        if (!(o instanceof TypeConge)) {
            return false;
        }
        return getId() != null && getId().equals(((TypeConge) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}