package app.domain.rh.situationFamiliale;

import jakarta.validation.constraints.NotNull;

public record SituationFamilialeDto(
        Long id,
        Long idSituationFamiliale,
        @NotNull String libelle,
        String code) {

    public static SituationFamilialeDto toDtoAsRef(SituationFamiliale entity) {
        return entity == null ? null : new SituationFamilialeDto(entity.getId(), entity.getIdSituationFamiliale(), entity.getLibelle(), null);
    }

    public static SituationFamiliale toEntityAsRef(SituationFamilialeDto dto) {
        if (dto == null) {
            return null;
        }

        Long id = dto.id() != null ? dto.id() : dto.idSituationFamiliale();
        if (id == null) {
            return null;
        }

        SituationFamiliale entity = new SituationFamiliale();
        entity.setId(id);
        return entity;
    }
}
