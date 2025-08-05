package app.core.notification;

import app.core.util.ReferenceDto;
import jakarta.persistence.Tuple;
import jakarta.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notification")
@Transactional
public class NotificationResource {

    private final NotificationRepository notificationRepository;

    public NotificationResource(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PostMapping
    public ResponseEntity<Notification> creerNotification(@Valid @RequestBody Notification notification) throws URISyntaxException {
        Notification result = notificationRepository.save(notification);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping
    public ResponseEntity<Notification> modifierNotification(@Valid @RequestBody Notification notification) throws URISyntaxException {
        Notification result = notificationRepository.save(notification);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping
    public List<Notification> listerNotification() {
        return notificationRepository.findAllByOrderById();
    }

    @GetMapping("/page")
    public Page<Notification> listerNotification(Pageable pageable) {
        return notificationRepository.findAllByOrderById(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> recupererNotification(@PathVariable Long id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        return notification.map(response -> ResponseEntity.ok().body(response)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerNotification(@PathVariable Long id) {
        notificationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/listeReference")
    public List<ReferenceDto> listerEnTantQueReference() {
        List<Tuple> list = notificationRepository.listeReference();
        List<ReferenceDto> dtoList = list.stream().map(t -> new ReferenceDto(t.get(0, Long.class), t.get(1, String.class))).collect(Collectors.toList());
        return dtoList;
    }
}
