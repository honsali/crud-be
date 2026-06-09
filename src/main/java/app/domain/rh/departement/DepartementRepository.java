package app.domain.rh.departement;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartementRepository extends JpaRepository<Departement, Long> {

    List<Departement> findAllByOrderByNom();
}
