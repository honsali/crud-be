package app.domain.rh.employe;

import java.time.LocalDate;
import app.domain.rh.departement.DepartementDto;
import app.domain.rh.sexe.SexeDto;
import app.domain.rh.situationFamiliale.SituationFamilialeDto;

public record EmployeDto(//
        Long id, //
        Long idEmploye, //
        String libelle, //
        String matricule, //
        String nom, //
        String prenom, //
        LocalDate dateNaissance, //
        SexeDto sexe, //
        SituationFamilialeDto situationFamiliale, //
        LocalDate dateEntree, //
        String email, //
        String telephone, //
        String ville, //
        String adresse, //
        String fonction, //
        String description, //
        DepartementDto departement //
) {

    public static EmployeDto toDto(Employe entity) {
        return entity == null ? null
                : new EmployeDto(//
                        entity.getId(), //
                        entity.getId(), //
                        entity.getDisplayString(), //
                        entity.getMatricule(), //
                        entity.getNom(), //
                        entity.getPrenom(), //
                        entity.getDateNaissance(), //
                        SexeDto.toDtoAsRef(entity.getSexe()), //
                        SituationFamilialeDto.toDtoAsRef(entity.getSituationFamiliale()), //
                        entity.getDateEntree(), //
                        entity.getEmail(), //
                        entity.getTelephone(), //
                        entity.getVille(), //
                        entity.getAdresse(), //
                        entity.getFonction(), //
                        entity.getDescription(), //
                        DepartementDto.toDtoAsRef(entity.getDepartement()) //
                );
    }

    public static EmployeDto toDtoAsRef(Employe entity) {
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
                        null, //
                        null, //
                        null //
                );
    }

    public static Employe toEntity(EmployeDto dto) {
        if (dto == null) {
            return null;
        }

        Employe entity = new Employe();
        entity.setMatricule(dto.matricule());
        entity.setNom(dto.nom());
        entity.setPrenom(dto.prenom());
        entity.setDateNaissance(dto.dateNaissance());
        entity.setSexe(SexeDto.toEntityAsRef(dto.sexe()));
        entity.setSituationFamiliale(SituationFamilialeDto.toEntityAsRef(dto.situationFamiliale()));
        entity.setDateEntree(dto.dateEntree());
        entity.setEmail(dto.email());
        entity.setTelephone(dto.telephone());
        entity.setVille(dto.ville());
        entity.setAdresse(dto.adresse());
        entity.setFonction(dto.fonction());
        entity.setDescription(dto.description());
        entity.setDepartement(DepartementDto.toEntityAsRef(dto.departement()));
        return entity;
    }

    public static Employe toEntity(EmployeDto dto, Long id) {
        Employe entity = toEntity(dto);
        if (entity != null) {
            entity.setId(id);
        }
        return entity;
    }

    public static Employe toEntityAsRef(EmployeDto dto) {
        if (dto == null || dto.id() == null) {
            return null;
        }

        Employe entity = new Employe();
        entity.setId(dto.id());
        return entity;
    }
}