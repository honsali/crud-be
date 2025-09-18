package app.domain.rh.conge;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CongeRepository extends JpaRepository<Conge, Long>, JpaSpecificationExecutor<Conge> {

    List<Conge> findAllByEmploye_IdOrderByCode(Long idEmploye);
}