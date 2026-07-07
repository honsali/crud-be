package app.domain.rh.employe;

import org.springframework.stereotype.Component;
import app.domain.rh.departement.DepartementMapper;
import app.domain.rh.sexe.SexeMapper;
import app.domain.rh.situationFamiliale.SituationFamilialeMapper;

@Component
public class EmployeMapper {

    private final DepartementMapper departementMapper;
    private final SexeMapper sexeMapper;
    private final SituationFamilialeMapper situationFamilialeMapper;

    public EmployeMapper(
            DepartementMapper departementMapper,
            SexeMapper sexeMapper,
            SituationFamilialeMapper situationFamilialeMapper) {
        this.departementMapper = departementMapper;
        this.sexeMapper = sexeMapper;
        this.situationFamilialeMapper = situationFamilialeMapper;
    }

    public EmployeDto toDto(Employe entity) {
        return entity == null ? null
                : new EmployeDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getMatricule(),
                        entity.getNom(),
                        entity.getPrenom(),
                        entity.getDateNaissance(),
                        sexeMapper.toDtoAsRef(entity.getSexe()),
                        situationFamilialeMapper.toDtoAsRef(entity.getSituationFamiliale()),
                        entity.getDateEntree(),
                        entity.getEmail(),
                        entity.getTelephone(),
                        entity.getVille(),
                        entity.getAdresse(),
                        entity.getFonction(),
                        entity.getDescription(),
                        departementMapper.toDtoAsRef(entity.getDepartement()));
    }

    public EmployeDto toDtoAsRef(Employe entity) {
        return entity == null ? null
                : new EmployeDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getMatricule(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }

    public Employe toEntity(EmployeDto dto) {
        if (dto == null) {
            return null;
        }

        Employe entity = new Employe();
        copyToEntity(dto, entity);
        return entity;
    }

    public void copyToEntity(EmployeDto dto, Employe entity) {
        entity.setMatricule(dto.matricule());
        entity.setNom(dto.nom());
        entity.setPrenom(dto.prenom());
        entity.setDateNaissance(dto.dateNaissance());
        entity.setSexe(sexeMapper.toEntityAsRef(dto.sexe()));
        entity.setSituationFamiliale(situationFamilialeMapper.toEntityAsRef(dto.situationFamiliale()));
        entity.setDateEntree(dto.dateEntree());
        entity.setEmail(dto.email());
        entity.setTelephone(dto.telephone());
        entity.setVille(dto.ville());
        entity.setAdresse(dto.adresse());
        entity.setFonction(dto.fonction());
        entity.setDescription(dto.description());
        entity.setDepartement(departementMapper.toEntityAsRef(dto.departement()));
    }
}
