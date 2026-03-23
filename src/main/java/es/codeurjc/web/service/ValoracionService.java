package es.codeurjc.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.codeurjc.web.model.Valoracion;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.repository.ValoracionRepository;
import java.util.Optional;

@Service
public class ValoracionService {

    @Autowired
    private ValoracionRepository valoracionRepository;

    public void save(Valoracion valoracion) {
        valoracionRepository.save(valoracion);
    }

    public Optional<Valoracion> findById(Long id) {
        return valoracionRepository.findById(id);
    }

    public Optional<Valoracion> findByAuthorAndConsejo(Usuario author, Consejo consejo) {
        return valoracionRepository.findByAuthorAndConsejo(author, consejo);
    }

    public void deleteById(Long id) {
        valoracionRepository.deleteById(id);
    }
}