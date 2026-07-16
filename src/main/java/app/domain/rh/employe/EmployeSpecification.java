package app.domain.rh.employe;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import app.core.persistence.BaseSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public final class EmployeSpecification extends BaseSpecification {

    private EmployeSpecification() {
    }

    public static Specification<Employe> buildSpecification(EmployeFiltre condition) {
        return (Root<Employe> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (condition == null) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addLike(predicates, criteriaBuilder, root.get("matricule"), condition.matricule());
            addLike(predicates, criteriaBuilder, root.get("nom"), condition.nom());
            addLike(predicates, criteriaBuilder, root.get("prenom"), condition.prenom());
            addDateRange(predicates, criteriaBuilder, root.get("dateNaissance"), condition.debutDateNaissance(), condition.finDateNaissance());
            addEqual(predicates, criteriaBuilder, root.get("sexe").get("id"), condition.sexe() == null ? null : condition.sexe().id());
            addEqual(predicates, criteriaBuilder, root.get("situationFamiliale").get("id"), condition.situationFamiliale() == null ? null : condition.situationFamiliale().id());
            addDateRange(predicates, criteriaBuilder, root.get("dateEntree"), condition.debutDateEntree(), condition.finDateEntree());
            addLike(predicates, criteriaBuilder, root.get("email"), condition.email());
            addLike(predicates, criteriaBuilder, root.get("telephone"), condition.telephone());
            addLike(predicates, criteriaBuilder, root.get("ville"), condition.ville());
            addLike(predicates, criteriaBuilder, root.get("adresse"), condition.adresse());
            addLike(predicates, criteriaBuilder, root.get("fonction"), condition.fonction());
            addLike(predicates, criteriaBuilder, root.get("description"), condition.description());
            addEqual(predicates, criteriaBuilder, root.get("departement").get("id"), condition.departement() == null ? null : condition.departement().id());

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
