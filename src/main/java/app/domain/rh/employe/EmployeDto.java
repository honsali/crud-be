package app.domain.rh.employe;

import java.time.LocalDate;
import app.domain.rh.departement.DepartementDto;

public record EmployeDto(//
        Long id, //
        Long idEmploye, //
        String libelle, //
        String matricule, //
        String nom, //
        String prenom, //
        LocalDate dateNaissance, //
        LocalDate dateEntree, //
        String email, //
        String telephone, //
        String ville, //
        String adresse, //
        String fonction, //
        String description, //
        DepartementDto departement //
) {

    public static EmployeDto asEntity(Employe entity) {
        return entity == null ? null
                : new EmployeDto(//
                        entity.getId(), //
                        entity.getId(), //
                        entity.getDisplayString(), //
                        entity.getMatricule(), //
                        entity.getNom(), //
                        entity.getPrenom(), //
                        entity.getDateNaissance(), //
                        entity.getDateEntree(), //
                        entity.getEmail(), //
                        entity.getTelephone(), //
                        entity.getVille(), //
                        entity.getAdresse(), //
                        entity.getFonction(), //
                        entity.getDescription(), //
                        DepartementDto.asRef(entity.getDepartement()) //
                );
    }

    public static EmployeDto asRef(Employe entity) {
        return entity == null ? null
                : new EmployeDto(//
                        entity.getId(), //
                        entity.getId(), //
                        entity.getDisplayString(), //
                        entity.getMatricule(), //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null, //
                        null //
                );
    }
}