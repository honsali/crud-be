package app.domain.rh.departement;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartementRepository extends JpaRepository<Departement, Long> {

    boolean existsByNom(String nom);

    List<Departement> findAllByOrderByNom();

    boolean existsByNomAndIdNot(String nom, Long id);
}
