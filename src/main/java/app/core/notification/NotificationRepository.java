package app.core.notification;

import jakarta.persistence.Tuple;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderById();

    Page<Notification> findAllByOrderById(Pageable pageable);

    @Query(value = "select id, libelle from Notification order by libelle")
    List<Tuple> listeReference();
}
