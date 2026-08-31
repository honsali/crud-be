package app.domain.rh.conge;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CongeRepository extends JpaRepository<Conge, Long> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = { "typeConge", "employe" })
    List<Conge> findAllByEmployeIdOrderByCode(Long idEmploye);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Override
    @EntityGraph(attributePaths = {"typeConge", "employe"})
    Optional<Conge> findById(Long id);
}
