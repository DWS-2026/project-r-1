package es.codeurjc.web.service;

import java.util.List;
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
import es.codeurjc.web.dto.ValoracionMapper;

@Service
public class ValoracionService {

    @Autowired
    private ValoracionRepository valoracionRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConsejoService consejoService;

    @Autowired
    private ValoracionMapper valoracionMapper;

    public ValoracionDTO toDTO(Valoracion v) {
        return valoracionMapper.toDTO(v);
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

    public List<Valoracion> findByAuthor(Usuario author) {
        return valoracionRepository.findByAuthor(author);
    }

    public Page<ValoracionDTO> findAll(Pageable pageable) {
        return valoracionRepository.findAll(pageable).map(valoracionMapper::toDTO);
    }

    public Valoracion createReview(Long consejoId, String userEmail, String title, int score, String comment) {
        Usuario user = usuarioService.findByEmail(userEmail).orElseThrow();
        Consejo consejo = consejoService.findById(consejoId).orElseThrow();

        // FIX BOLA: Evita que alguien valore algo que no ha comprado.
        boolean hasBought = user.getCompras().stream().anyMatch(t -> t.getConsejo().getId().equals(consejoId));
        boolean isSeller = consejo.getSeller().getId().equals(user.getId());

        if (!hasBought && !isSeller) {
            throw new SecurityException("Acceso denegado: No puedes valorar un consejo que no has adquirido.");
        }

        Valoracion v = new Valoracion(user, consejo, score, title, comment);
        return valoracionRepository.save(v);
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

    // FIX BAC (Broken Access Control): Se añade validación para que el ADMIN también pueda borrar reseñas.
    public boolean deleteReview(Long id, String userEmail) {
        Optional<Valoracion> vOpcional = valoracionRepository.findById(id);
        if (vOpcional.isPresent()) {
            Usuario user = usuarioService.findByEmail(userEmail).orElseThrow();
            boolean isOwner = vOpcional.get().getAuthor().getEmail().equals(userEmail);
            boolean isAdmin = user.getRoles().contains("ADMIN");
            
            if (isOwner || isAdmin) {
                valoracionRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }
}