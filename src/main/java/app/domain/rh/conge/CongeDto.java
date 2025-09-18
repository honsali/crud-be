package app.domain.rh.conge;

import java.time.LocalDate;
import app.domain.rh.employe.EmployeDto;
import app.domain.rh.typeConge.TypeCongeDto;

public record CongeDto(//
        Long id, //
        Long idConge, //
        String libelle, //
        String code, //
        TypeCongeDto typeConge, //
        LocalDate dateDebutConge, //
        LocalDate dateFinConge, //
        String commentaire, //
        EmployeDto employe //
) {

    public static CongeDto toDto(Conge entity) {
        return entity == null ? null
                : new CongeDto(//
                        entity.getId(), //
                        entity.getId(), //
                        entity.getDisplayString(), //
                        entity.getCode(), //
                        TypeCongeDto.toDtoAsRef(entity.getTypeConge()), //
                        entity.getDateDebutConge(), //
                        entity.getDateFinConge(), //
                        entity.getCommentaire(), //
                        EmployeDto.toDtoAsRef(entity.getEmploye()) //
                );
    }

    public static CongeDto toDtoAsRef(Conge entity) {
        return entity == null ? null
                : new CongeDto(//
                        entity.getId(), //
                        entity.getId(), //
                        entity.getDisplayString(), //
                        entity.getCode(), //
                        null, //
                        null, //
                        null, //
                        null, //
                        null //
                );
    }

    public static Conge toEntity(CongeDto dto) {
        if (dto == null) {
            return null;
        }

        Conge entity = new Conge();
        entity.setCode(dto.code());
        entity.setTypeConge(TypeCongeDto.toEntityAsRef(dto.typeConge()));
        entity.setDateDebutConge(dto.dateDebutConge());
        entity.setDateFinConge(dto.dateFinConge());
        entity.setCommentaire(dto.commentaire());
        entity.setEmploye(EmployeDto.toEntityAsRef(dto.employe()));
        return entity;
    }

    public static Conge toEntity(CongeDto dto, Long id) {
        Conge entity = toEntity(dto);
        if (entity != null) {
            entity.setId(id);
        }
        return entity;
    }

    public static Conge toEntityAsRef(CongeDto dto) {
        if (dto == null || dto.id() == null) {
            return null;
        }

        Conge entity = new Conge();
        entity.setId(dto.id());
        return entity;
    }
}