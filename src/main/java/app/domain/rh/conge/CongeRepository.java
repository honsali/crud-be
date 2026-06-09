package app.domain.rh.conge;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CongeRepository extends JpaRepository<Conge, Long> {

    List<Conge> findAllByEmploye_IdOrderByCode(Long idEmploye);
}
