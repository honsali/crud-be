package app.domain.rh.conge;

import org.springframework.stereotype.Component;
import app.domain.rh.employe.EmployeMapper;
import app.domain.rh.typeConge.TypeCongeMapper;

@Component
public class CongeMapper {

    private final EmployeMapper employeMapper;
    private final TypeCongeMapper typeCongeMapper;

    public CongeMapper(EmployeMapper employeMapper, TypeCongeMapper typeCongeMapper) {
        this.employeMapper = employeMapper;
        this.typeCongeMapper = typeCongeMapper;
    }

    public CongeDto toDto(Conge entity) {
        return entity == null ? null
                : new CongeDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getCode(),
                        typeCongeMapper.toDtoAsRef(entity.getTypeConge()),
                        entity.getDateDebutConge(),
                        entity.getDateFinConge(),
                        entity.getCommentaire(),
                        employeMapper.toDtoAsRef(entity.getEmploye()));
    }

    public CongeDto toDtoAsRef(Conge entity) {
        return entity == null ? null
                : new CongeDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getCode(),
                        null,
                        null,
                        null,
                        null,
                        null);
    }

    public Conge toEntity(CongeDto dto) {
        if (dto == null) {
            return null;
        }

        Conge entity = new Conge();
        copyToEntity(dto, entity);
        return entity;
    }

    public void copyToEntity(CongeDto dto, Conge entity) {
        entity.setCode(dto.code());
        entity.setTypeConge(typeCongeMapper.toEntityAsRef(dto.typeConge()));
        entity.setDateDebutConge(dto.dateDebutConge());
        entity.setDateFinConge(dto.dateFinConge());
        entity.setCommentaire(dto.commentaire());
    }
}
