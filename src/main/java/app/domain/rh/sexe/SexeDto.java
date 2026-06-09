package app.domain.rh.sexe;

import jakarta.validation.constraints.NotNull;

public record SexeDto(
        Long id,
        Long idSexe,
        @NotNull String libelle,
        String code) {

    public static SexeDto toDtoAsRef(Sexe entity) {
        return entity == null ? null : new SexeDto(entity.getId(), entity.getIdSexe(), entity.getLibelle(), null);
    }

    public static Sexe toEntityAsRef(SexeDto dto) {
        if (dto == null) {
            return null;
        }

        Long id = dto.id() != null ? dto.id() : dto.idSexe();
        if (id == null) {
            return null;
        }

        Sexe entity = new Sexe();
        entity.setId(id);
        return entity;
    }
}
