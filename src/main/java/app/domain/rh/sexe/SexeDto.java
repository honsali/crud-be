package app.domain.rh.sexe;

import jakarta.validation.constraints.NotNull;

public record SexeDto(//
        Long id, //
        Long idSexe, //
        @NotNull
        String libelle, //
        @NotNull
        String code //
) {

    public static SexeDto toDtoAsRef(Sexe entity) {
        return entity == null ? null
                : new SexeDto(//
                        entity.getId(), //
                        entity.getId(), //
                        entity.getLibelle(), //
                        null //
                );
    }

    public static Sexe toEntityAsRef(SexeDto dto) {
        if (dto == null || dto.id() == null) {
            return null;
        }

        Sexe entity = new Sexe();
        entity.setId(dto.id());
        return entity;
    }
}