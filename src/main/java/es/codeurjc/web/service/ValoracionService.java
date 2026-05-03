package es.codeurjc.web.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.model.Valoracion;
import es.codeurjc.web.repository.ValoracionRepository;
import es.codeurjc.web.dto.ValoracionDTO;

@Service
public class ValoracionService {

    @Autowired
    private ValoracionRepository valoracionRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConsejoService consejoService;

    // --- Métodos de conversión a DTO ---
    public ValoracionDTO toDTO(Valoracion v) {
        return new ValoracionDTO(
                v.getId(),
                v.getAuthor() != null ? v.getAuthor().getId() : null,
                v.getAuthor() != null ? v.getAuthor().getNombre() : "Unknown",
                v.getConsejo() != null ? v.getConsejo().getId() : null,
                v.getConsejo() != null ? v.getConsejo().getTitle() : "Unknown",
                v.getScore(),
                v.getTitle(),
                v.getComment()
        );
    }

    public void save(Valoracion valoracion) {
        valoracionRepository.save(valoracion);
    }

    public Optional<Valoracion> findById(Long id) {
        return valoracionRepository.findById(id);
    }

    public Optional<Valoracion> findByAuthorAndConsejo(Usuario author, Consejo consejo) {
        return valoracionRepository.findByAuthorAndConsejo(author, consejo);
    }

    // Nuevo método para soportar paginación en la API REST
    public Page<ValoracionDTO> findAll(Pageable pageable) {
        return valoracionRepository.findAll(pageable).map(this::toDTO);
    }

    public void createReview(Long consejoId, String userEmail, String title, int score, String comment) {
        Usuario user = usuarioService.findByEmail(userEmail).orElseThrow();
        Consejo consejo = consejoService.findById(consejoId).orElseThrow();
        Valoracion v = new Valoracion(user, consejo, score, title, comment);
        valoracionRepository.save(v);
    }

    public boolean updateReview(Long id, String userEmail, String title, int score, String comment) {
        Optional<Valoracion> vOpcional = valoracionRepository.findById(id);
        if (vOpcional.isPresent() && vOpcional.get().getAuthor().getEmail().equals(userEmail)) {
            Valoracion v = vOpcional.get();
            v.setTitle(title);
            v.setScore(score);
            v.setComment(comment);
            valoracionRepository.save(v);
            return true;
        }
        return false;
    }

    public boolean deleteReview(Long id, String userEmail) {
        Optional<Valoracion> vOpcional = valoracionRepository.findById(id);
        if (vOpcional.isPresent() && vOpcional.get().getAuthor().getEmail().equals(userEmail)) {
            valoracionRepository.deleteById(id);
            return true;
        }
        return false;
    }
}