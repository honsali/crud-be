package app.domain.rh.employe;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class EmployeSpecification {

    public static Specification<Employe> buildSpecification(EmployeFiltre condition) {
        return (Root<Employe> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (condition.getMatricule() != null && !condition.getMatricule().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("matricule")), "%" + condition.getMatricule().toLowerCase() + "%"));
            }

            if (condition.getNom() != null && !condition.getNom().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), "%" + condition.getNom().toLowerCase() + "%"));
            }

            if (condition.getPrenom() != null && !condition.getPrenom().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("prenom")), "%" + condition.getPrenom().toLowerCase() + "%"));
            }

            if (condition.getDebutDateNaissance() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateNaissance"), condition.getDebutDateNaissance()));
            }

            if (condition.getFinDateNaissance() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateNaissance"), condition.getFinDateNaissance()));
            }

            if (condition.getSexe() != null && condition.getSexe().id() != null) {
                predicates.add(criteriaBuilder.equal(root.get("sexe").get("id"), condition.getSexe().id()));
            }

            if (condition.getSituationFamiliale() != null && condition.getSituationFamiliale().id() != null) {
                predicates.add(criteriaBuilder.equal(root.get("situationFamiliale").get("id"), condition.getSituationFamiliale().id()));
            }

            if (condition.getDebutDateEntree() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateEntree"), condition.getDebutDateEntree()));
            }

            if (condition.getFinDateEntree() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateEntree"), condition.getFinDateEntree()));
            }

            if (condition.getEmail() != null && !condition.getEmail().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + condition.getEmail().toLowerCase() + "%"));
            }

            if (condition.getTelephone() != null && !condition.getTelephone().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("telephone")), "%" + condition.getTelephone().toLowerCase() + "%"));
            }

            if (condition.getVille() != null && !condition.getVille().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("ville")), "%" + condition.getVille().toLowerCase() + "%"));
            }

            if (condition.getAdresse() != null && !condition.getAdresse().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("adresse")), "%" + condition.getAdresse().toLowerCase() + "%"));
            }

            if (condition.getFonction() != null && !condition.getFonction().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("fonction")), "%" + condition.getFonction().toLowerCase() + "%"));
            }

            if (condition.getDescription() != null && !condition.getDescription().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + condition.getDescription().toLowerCase() + "%"));
            }

            if (condition.getDepartement() != null && condition.getDepartement().id() != null) {
                predicates.add(criteriaBuilder.equal(root.get("departement").get("id"), condition.getDepartement().id()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}