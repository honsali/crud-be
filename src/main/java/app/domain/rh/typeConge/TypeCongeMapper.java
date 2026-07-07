package app.domain.rh.typeConge;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;

@Component
public class TypeCongeMapper {

    private final TypeCongeRepository typeCongeRepository;

    public TypeCongeMapper(TypeCongeRepository typeCongeRepository) {
        this.typeCongeRepository = typeCongeRepository;
    }

    public TypeCongeDto toDtoAsRef(TypeConge entity) {
        return entity == null ? null
                : new TypeCongeDto(
                        entity.getId(),
                        entity.getId(),
                        entity.getLibelle(),
                        null);
    }

    public TypeConge toEntityAsRef(TypeCongeDto dto) {
        if (dto == null || dto.id() == null) {
            return null;
        }
        return typeCongeRepository.findById(dto.id()).orElseThrow(() -> new NoSuchElementException("TypeConge not found"));
    }
}
