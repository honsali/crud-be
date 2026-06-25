package app.domain.rh.conge;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CongeRepository extends JpaRepository<Conge, Long> {

    boolean existsByCode(String code);

    @EntityGraph(attributePaths = { "typeConge", "employe" })
    List<Conge> findAllByEmploye_IdOrderByCode(Long idEmploye);

    boolean existsByCodeAndIdNot(String code, Long id);
}
