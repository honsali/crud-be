package app.domain.rh.employe;

import app.core.reference.Reference;
import app.domain.rh.departement.Departement;
import app.domain.rh.departement.DepartementMapper;
import app.domain.rh.sexe.Sexe;
import app.domain.rh.sexe.SexeMapper;
import app.domain.rh.situationFamiliale.SituationFamiliale;
import app.domain.rh.situationFamiliale.SituationFamilialeMapper;

public final class EmployeMapper {

    public static EmployeResponse toResponse(Employe employe) {
        return new EmployeResponse(
                employe.getId(),
                employe.getMatricule(),
                employe.getNom(),
                employe.getPrenom(),
                employe.getDateNaissance(),
                employe.getSexe() == null ? null : SexeMapper.toReference(employe.getSexe()),
                employe.getSituationFamiliale() == null ? null : SituationFamilialeMapper.toReference(employe.getSituationFamiliale()),
                employe.getDateEntree(),
                employe.getEmail(),
                employe.getTelephone(),
                employe.getVille(),
                employe.getAdresse(),
                employe.getFonction(),
                employe.getDescription(),
                employe.getDepartement() == null ? null : DepartementMapper.toReference(employe.getDepartement()),
                employe.getVersion());
    }

    public static Employe toEntity(EmployeCreateRequest request, Sexe sexe, SituationFamiliale situationFamiliale, Departement departement) {
        return new Employe(
                request.matricule(),
                request.nom(),
                request.prenom(),
                request.dateNaissance(),
                sexe,
                situationFamiliale,
                request.dateEntree(),
                request.email(),
                request.telephone(),
                request.ville(),
                request.adresse(),
                request.fonction(),
                request.description(),
                departement);
    }

    public static void toEntity(Employe employe, EmployeUpdateRequest request, Sexe sexe, SituationFamiliale situationFamiliale, Departement departement) {
        employe.update(
                request.matricule(),
                request.nom(),
                request.prenom(),
                request.dateNaissance(),
                sexe,
                situationFamiliale,
                request.dateEntree(),
                request.email(),
                request.telephone(),
                request.ville(),
                request.adresse(),
                request.fonction(),
                request.description(),
                departement);
    }

    public static Reference toReference(Employe employe) {
        return new Reference(employe.getId(), employe.getMatricule());
    }

    private EmployeMapper() {}
}
