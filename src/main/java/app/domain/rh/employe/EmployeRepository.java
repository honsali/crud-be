package app.domain.rh.employe;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeRepository extends JpaRepository<Employe, Long>, JpaSpecificationExecutor<Employe> {

    boolean existsByMatricule(String matricule);

    boolean existsByMatriculeAndIdNot(String matricule, Long id);

    @Override
    @EntityGraph(attributePaths = {"sexe", "situationFamiliale", "departement"})
    Optional<Employe> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"sexe", "situationFamiliale", "departement"})
    Page<Employe> findAll(Specification<Employe> specification, Pageable pageable);
}
