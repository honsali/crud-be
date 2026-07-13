package app.domain.rh.referenceData;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import app.core.referenceData.ReferenceDataCatalog;
import app.core.referenceData.ReferenceDataDefinition;

@Component
public class RhReferenceDataCatalog implements ReferenceDataCatalog {

    private static final Map<String, ReferenceDataDefinition> REFERENCES = Map.of(
            "departement", new ReferenceDataDefinition("Departement", "nom", Set.of("id")),
            "employe", new ReferenceDataDefinition("Employe", "matricule", Set.of("id", "departement.id", "sexe.id", "situationFamiliale.id")),
            "sexe", new ReferenceDataDefinition("Sexe", "libelle", Set.of("id")),
            "situationfamiliale", new ReferenceDataDefinition("SituationFamiliale", "libelle", Set.of("id")),
            "typeconge", new ReferenceDataDefinition("TypeConge", "libelle", Set.of("id")));

    @Override
    public Optional<ReferenceDataDefinition> find(String referenceName) {
        return Optional.ofNullable(REFERENCES.get(referenceName.toLowerCase(Locale.ROOT)));
    }
}
