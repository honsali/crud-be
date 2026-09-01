package app.domain.rh.employe;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import app.core.persistence.BaseEntity;
import app.domain.rh.departement.Departement;
import app.domain.rh.sexe.Sexe;
import app.domain.rh.situationfamiliale.SituationFamiliale;

@Entity
@Table(name = "employe")
public class Employe extends BaseEntity {

    private String matricule;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sexe_id")
    private Sexe sexe;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "situation_familiale_id")
    private SituationFamiliale situationFamiliale;
    private LocalDate dateEntree;
    private String email;
    private String telephone;
    private String ville;
    private String adresse;
    private String fonction;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    protected Employe() {}

    Employe(
            String matricule,
            String nom,
            String prenom,
            LocalDate dateNaissance,
            Sexe sexe,
            SituationFamiliale situationFamiliale,
            LocalDate dateEntree,
            String email,
            String telephone,
            String ville,
            String adresse,
            String fonction,
            String description,
            Departement departement) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.sexe = sexe;
        this.situationFamiliale = situationFamiliale;
        this.dateEntree = dateEntree;
        this.email = email;
        this.telephone = telephone;
        this.ville = ville;
        this.adresse = adresse;
        this.fonction = fonction;
        this.description = description;
        this.departement = departement;
    }

    public String getMatricule() {
        return matricule;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public Sexe getSexe() {
        return sexe;
    }

    public SituationFamiliale getSituationFamiliale() {
        return situationFamiliale;
    }

    public LocalDate getDateEntree() {
        return dateEntree;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getVille() {
        return ville;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getFonction() {
        return fonction;
    }

    public String getDescription() {
        return description;
    }

    public Departement getDepartement() {
        return departement;
    }

    public void update(
            String matricule,
            String nom,
            String prenom,
            LocalDate dateNaissance,
            Sexe sexe,
            SituationFamiliale situationFamiliale,
            LocalDate dateEntree,
            String email,
            String telephone,
            String ville,
            String adresse,
            String fonction,
            String description,
            Departement departement) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.sexe = sexe;
        this.situationFamiliale = situationFamiliale;
        this.dateEntree = dateEntree;
        this.email = email;
        this.telephone = telephone;
        this.ville = ville;
        this.adresse = adresse;
        this.fonction = fonction;
        this.description = description;
        this.departement = departement;
    }
}
