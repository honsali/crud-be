package app.core.referenceData;

import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ReferenceDataService {

    private static final HashMap<String, String> fieldMap = new HashMap<>();
    static {
        fieldMap.put("Departement", "nom");
    }
    @PersistenceContext
    private EntityManager entityManager;

    public List<ReferenceDataDto> getReferenceData(String entityName) {
        String entityNameCapitalized = StringUtils.capitalize(entityName);
        String fieldName = fieldMap.get(entityNameCapitalized);
        fieldName = fieldName == null ? "libelle" : fieldName;
        String query = String.format("SELECT new app.core.referenceData.ReferenceDataDto(e.id, e.%s) FROM %s e", fieldName, entityNameCapitalized);
        return entityManager.createQuery(query, ReferenceDataDto.class).getResultList();
    }

    public List<ReferenceDataDto> getReferenceData(String entityName, String field, Long value) {
        String entityNameCapitalized = StringUtils.capitalize(entityName);
        String fieldName = fieldMap.get(entityNameCapitalized);
        fieldName = fieldName == null ? "libelle" : fieldName;
        String query = String.format("SELECT new app.core.referenceData.ReferenceDataDto(e.id, e.%s) FROM %s e WHERE e.%s = :value", fieldName, entityNameCapitalized, field);
        return entityManager.createQuery(query, ReferenceDataDto.class).setParameter("value", value).getResultList();
    }

    public ReferenceDataDto getReferenceData(String entityName, Long entityId) {
        String entityNameCapitalized = StringUtils.capitalize(entityName);
        String fieldName = fieldMap.get(entityNameCapitalized);
        fieldName = fieldName == null ? "libelle" : fieldName;
        String query = String.format("SELECT new app.core.referenceData.ReferenceDataDto(e.id, e.%s) FROM %s e WHERE e.id = :id", fieldName, entityNameCapitalized);
        return entityManager.createQuery(query, ReferenceDataDto.class).setParameter("id", entityId).getSingleResult();
    }
}
