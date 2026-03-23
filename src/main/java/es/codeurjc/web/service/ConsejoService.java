package es.codeurjc.web.service;
import java.util.Optional;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.ConsejoRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsejoService {

    @Autowired
    private ConsejoRepository consejoRepository;

    public void saveConsejo(Consejo consejo) {
        consejoRepository.save(consejo);
    }

    public List<Consejo> findAll() { return consejoRepository.findAll(); }

    public Optional<Consejo> findById(Long id) {
        return consejoRepository.findById(id);
    }
    public List<Consejo> findBySeller(Usuario seller) {
        return consejoRepository.findBySeller(seller);
    }
    public void deleteById(Long id) {
        consejoRepository.deleteById(id);
    }
}