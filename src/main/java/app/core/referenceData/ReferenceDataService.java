package app.core.referenceData;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

@Service
@Transactional(readOnly = true)
public class ReferenceDataService {

    private final EntityManager entityManager;
    private final ReferenceDataCatalog referenceDataCatalog;

    public ReferenceDataService(EntityManager entityManager, ReferenceDataCatalog referenceDataCatalog) {
        this.entityManager = entityManager;
        this.referenceDataCatalog = referenceDataCatalog;
    }

    public List<ReferenceDataDto> getReferenceData(String entityName) {
        ReferenceDataDefinition metadata = metadata(entityName);
        String query = "SELECT new app.core.referenceData.ReferenceDataDto(e.id, e.%s) FROM %s e ORDER BY e.%s".formatted(metadata.labelField(), metadata.entityName(), metadata.labelField());
        return entityManager.createQuery(query, ReferenceDataDto.class).getResultList();
    }

    public List<ReferenceDataDto> getReferenceData(String entityName, String field, Long value) {
        ReferenceDataDefinition metadata = metadata(entityName);
        if (!metadata.filterFields().contains(field)) {
            throw new IllegalArgumentException("Unsupported reference filter: " + field);
        }

        String query = "SELECT new app.core.referenceData.ReferenceDataDto(e.id, e.%s) FROM %s e WHERE e.%s = :value ORDER BY e.%s".formatted(metadata.labelField(), metadata.entityName(), field, metadata.labelField());
        return entityManager.createQuery(query, ReferenceDataDto.class).setParameter("value", value).getResultList();
    }

    public ReferenceDataDto getReferenceData(String entityName, Long entityId) {
        ReferenceDataDefinition metadata = metadata(entityName);
        String query = "SELECT new app.core.referenceData.ReferenceDataDto(e.id, e.%s) FROM %s e WHERE e.id = :id".formatted(metadata.labelField(), metadata.entityName());
        return entityManager.createQuery(query, ReferenceDataDto.class).setParameter("id", entityId).getResultStream().findFirst().orElseThrow(() -> new NoSuchElementException("Reference data not found"));
    }

    private ReferenceDataDefinition metadata(String entityName) {
        return referenceDataCatalog.find(entityName).orElseThrow(() -> new IllegalArgumentException("Unsupported reference entity: " + entityName));
    }
}
