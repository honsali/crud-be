package app.domain.rh.departement;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;

@Component
public class DepartementMapper {

    private final DepartementRepository departementRepository;

    public DepartementMapper(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    public DepartementDto toDto(Departement entity) {
        return entity == null ? null
                : new DepartementDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getNom(),
                        entity.getDescription());
    }

    public DepartementDto toDtoAsRef(Departement entity) {
        return entity == null ? null
                : new DepartementDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getNom(),
                        null);
    }

    public Departement toEntity(DepartementDto dto) {
        if (dto == null) {
            return null;
        }

        Departement entity = new Departement();
        copyToEntity(dto, entity);
        return entity;
    }

    public Departement toEntityAsRef(DepartementDto dto) {
        if (dto == null || dto.id() == null) {
            return null;
        }
        return departementRepository.findById(dto.id()).orElseThrow(() -> new NoSuchElementException("Departement not found"));
    }

    public void copyToEntity(DepartementDto dto, Departement entity) {
        entity.setNom(dto.nom());
        entity.setDescription(dto.description());
    }
}
