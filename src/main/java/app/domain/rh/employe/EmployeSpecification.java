package app.domain.rh.employe;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import app.core.persistence.BaseSpecification;

public final class EmployeSpecification extends BaseSpecification {

    public static Specification<Employe> buildSpecification(EmployeFiltre filtre) {
        return (root, query, builder) -> {
            if (filtre == null) {
                return builder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addLike(predicates, builder, root.get("matricule"), filtre.matricule());
            addLike(predicates, builder, root.get("nom"), filtre.nom());
            addLike(predicates, builder, root.get("prenom"), filtre.prenom());
            addDateRange(
                    predicates,
                    builder,
                    root.get("dateNaissance"),
                    filtre.debutDateNaissance(),
                    filtre.finDateNaissance());
            addReference(predicates, builder, root, "sexe", filtre.sexe());
            addReference(predicates, builder, root, "situationFamiliale", filtre.situationFamiliale());
            addDateRange(
                    predicates,
                    builder,
                    root.get("dateEntree"),
                    filtre.debutDateEntree(),
                    filtre.finDateEntree());
            addLike(predicates, builder, root.get("email"), filtre.email());
            addLike(predicates, builder, root.get("telephone"), filtre.telephone());
            addLike(predicates, builder, root.get("ville"), filtre.ville());
            addLike(predicates, builder, root.get("adresse"), filtre.adresse());
            addLike(predicates, builder, root.get("fonction"), filtre.fonction());
            addLike(predicates, builder, root.get("description"), filtre.description());
            addReference(predicates, builder, root, "departement", filtre.departement());

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private EmployeSpecification() {}
}
