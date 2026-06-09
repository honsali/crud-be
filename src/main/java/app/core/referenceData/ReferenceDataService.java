package app.core.referenceData;

import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ReferenceDataService {

    private record ReferenceMetadata(String entityName, String labelField, Set<String> filterFields) {
    }

    private static final Map<String, ReferenceMetadata> REFERENCES = Map.of(
            "departement", new ReferenceMetadata("Departement", "nom", Set.of("id")),
            "employe", new ReferenceMetadata("Employe", "matricule", Set.of("id", "departement.id", "sexe.id", "situationFamiliale.id")),
            "sexe", new ReferenceMetadata("Sexe", "libelle", Set.of("id")),
            "situationfamiliale", new ReferenceMetadata("SituationFamiliale", "libelle", Set.of("id")),
            "situationFamiliale", new ReferenceMetadata("SituationFamiliale", "libelle", Set.of("id")),
            "typeconge", new ReferenceMetadata("TypeConge", "libelle", Set.of("id")),
            "typeConge", new ReferenceMetadata("TypeConge", "libelle", Set.of("id")));

    @PersistenceContext
    private EntityManager entityManager;

    public java.util.List<ReferenceDataDto> getReferenceData(String entityName) {
        ReferenceMetadata metadata = metadata(entityName);
        String query = "SELECT new app.core.referenceData.ReferenceDataDto(e.id, e.%s) FROM %s e ORDER BY e.%s"
                .formatted(metadata.labelField(), metadata.entityName(), metadata.labelField());
        return entityManager.createQuery(query, ReferenceDataDto.class).getResultList();
    }

    public java.util.List<ReferenceDataDto> getReferenceData(String entityName, String field, Long value) {
        ReferenceMetadata metadata = metadata(entityName);
        if (!metadata.filterFields().contains(field)) {
            throw new IllegalArgumentException("Unsupported reference filter: " + field);
        }
        String query = "SELECT new app.core.referenceData.ReferenceDataDto(e.id, e.%s) FROM %s e WHERE e.%s = :value ORDER BY e.%s"
                .formatted(metadata.labelField(), metadata.entityName(), field, metadata.labelField());
        return entityManager.createQuery(query, ReferenceDataDto.class).setParameter("value", value).getResultList();
    }

    public ReferenceDataDto getReferenceData(String entityName, Long entityId) {
        ReferenceMetadata metadata = metadata(entityName);
        String query = "SELECT new app.core.referenceData.ReferenceDataDto(e.id, e.%s) FROM %s e WHERE e.id = :id"
                .formatted(metadata.labelField(), metadata.entityName());
        return entityManager.createQuery(query, ReferenceDataDto.class).setParameter("id", entityId).getSingleResult();
    }

    private ReferenceMetadata metadata(String entityName) {
        ReferenceMetadata metadata = REFERENCES.get(entityName);
        if (metadata == null) {
            metadata = REFERENCES.get(entityName.toLowerCase());
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Unsupported reference entity: " + entityName);
        }
        return metadata;
    }
}
