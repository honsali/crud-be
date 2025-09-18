package app.domain.rh.employe;

import java.time.LocalDate;
import app.domain.rh.departement.DepartementDto;
import app.domain.rh.sexe.SexeDto;
import app.domain.rh.situationFamiliale.SituationFamilialeDto;

public class EmployeFiltre {

    private String matricule;
    private String nom;
    private String prenom;
    private LocalDate debutDateNaissance;
    private LocalDate finDateNaissance;
    private SexeDto sexe;
    private SituationFamilialeDto situationFamiliale;
    private LocalDate debutDateEntree;
    private LocalDate finDateEntree;
    private String email;
    private String telephone;
    private String ville;
    private String adresse;
    private String fonction;
    private String description;
    private DepartementDto departement;


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

    public LocalDate getDebutDateNaissance() {
        return this.debutDateNaissance;
    }

    public void setDebutDateNaissance(LocalDate debutDateNaissance) {
        this.debutDateNaissance = debutDateNaissance;
    }

    public LocalDate getFinDateNaissance() {
        return this.finDateNaissance;
    }

    public void setFinDateNaissance(LocalDate finDateNaissance) {
        this.finDateNaissance = finDateNaissance;
    }

    public SexeDto getSexe() {
        return this.sexe;
    }

    public void setSexe(SexeDto sexe) {
        this.sexe = sexe;
    }

    public SituationFamilialeDto getSituationFamiliale() {
        return this.situationFamiliale;
    }

    public void setSituationFamiliale(SituationFamilialeDto situationFamiliale) {
        this.situationFamiliale = situationFamiliale;
    }

    public LocalDate getDebutDateEntree() {
        return this.debutDateEntree;
    }

    public void setDebutDateEntree(LocalDate debutDateEntree) {
        this.debutDateEntree = debutDateEntree;
    }

    public LocalDate getFinDateEntree() {
        return this.finDateEntree;
    }

    public void setFinDateEntree(LocalDate finDateEntree) {
        this.finDateEntree = finDateEntree;
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

    public DepartementDto getDepartement() {
        return this.departement;
    }

    public void setDepartement(DepartementDto departement) {
        this.departement = departement;
    }
}