package app.domain.rh.employe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeRepository extends JpaRepository<Employe, Long>, JpaSpecificationExecutor<Employe> {
}
