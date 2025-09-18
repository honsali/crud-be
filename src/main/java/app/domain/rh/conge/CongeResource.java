package app.domain.rh.conge;

import java.net.URISyntaxException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

@RestController
public class CongeResource {

    private final CongeService congeService;

    public CongeResource(CongeService congeService) {
        this.congeService = congeService;
    }

    @GetMapping("/api/conge/employe/{idEmploye}")
    public List<CongeDto> listerParIdEmploye(@PathVariable Long idEmploye) {
        return congeService.listerParIdEmploye(idEmploye);
    }

    @PutMapping("/api/conge/{id}")
    public ResponseEntity<CongeDto> maj(@PathVariable Long id, @Valid @RequestBody CongeDto congeDto) throws URISyntaxException {
        try {
            if (congeDto.id() != null && !congeDto.id().equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path ID and body ID mismatch");
            }
            CongeDto result = congeService.maj(id, congeDto);
            return ResponseEntity.ok().body(result);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/api/conge/{id}")
    public ResponseEntity<CongeDto> recupererParId(@PathVariable Long id) {
        return congeService.recupererParId(id).map(dto -> ResponseEntity.ok().body(dto)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conge not found"));
    }
}