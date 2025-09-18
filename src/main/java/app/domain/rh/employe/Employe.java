package app.domain.rh.employe;

import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import app.domain.rh.departement.Departement;
import app.domain.rh.sexe.Sexe;
import app.domain.rh.situationFamiliale.SituationFamiliale;
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
@Table(name = "employe")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Employe implements Serializable {

    private static final long serialVersionUID = 1311388895L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_employe")
    @SequenceGenerator(name = "seq_employe", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "matricule")
    private String matricule;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @ManyToOne(fetch = FetchType.LAZY)
    private Sexe sexe;

    @ManyToOne(fetch = FetchType.LAZY)
    private SituationFamiliale situationFamiliale;

    @Column(name = "date_entree")
    private LocalDate dateEntree;

    @Column(name = "email")
    private String email;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "ville")
    private String ville;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "fonction")
    private String fonction;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Departement departement;

    public Long getId() {
        return this.id;
    }

    public Employe id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayString() {
        return matricule;
    }

    public Long getIdEmploye() {
        return this.id;
    }

    public String getMatricule() {
        return this.matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return this.prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDateNaissance() {
        return this.dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public Sexe getSexe() {
        return this.sexe;
    }

    public void setSexe(Sexe sexe) {
        this.sexe = sexe;
    }

    public SituationFamiliale getSituationFamiliale() {
        return this.situationFamiliale;
    }

    public void setSituationFamiliale(SituationFamiliale situationFamiliale) {
        this.situationFamiliale = situationFamiliale;
    }

    public LocalDate getDateEntree() {
        return this.dateEntree;
    }

    public void setDateEntree(LocalDate dateEntree) {
        this.dateEntree = dateEntree;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getVille() {
        return this.ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getAdresse() {
        return this.adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getFonction() {
        return this.fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Departement getDepartement() {
        return this.departement;
    }

    public void setDepartement(Departement departement) {
        this.departement = departement;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Employe)) {
            return false;
        }
        return getId() != null && getId().equals(((Employe) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}