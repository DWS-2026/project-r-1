package es.codeurjc.web.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.ConsejoRepository;

@Service
public class ConsejoService {

    @Autowired
    private ConsejoRepository consejoRepository;

    @Autowired
    private UsuarioService usuarioService;

    public void saveConsejo(Consejo consejo) {
        consejoRepository.save(consejo);
    }

    public List<Consejo> findAll() { 
        return consejoRepository.findAll(); 
    }

    public Optional<Consejo> findById(Long id) {
        return consejoRepository.findById(id);
    }

    public List<Consejo> findBySeller(Usuario seller) {
        return consejoRepository.findBySeller(seller);
    }

    public void createAdvice(Consejo consejo, String sellerEmail, MultipartFile imageFile) throws IOException {
        Usuario seller = usuarioService.findByEmail(sellerEmail).orElseThrow();
        consejo.setSeller(seller);
        if (!imageFile.isEmpty()) {
            consejo.setImageBytes(imageFile.getBytes());
        }
        consejoRepository.save(consejo);
    }

    public boolean deleteAdvice(Long id, String userEmail) {
        Optional<Consejo> consejo = consejoRepository.findById(id);
        if (consejo.isPresent() && consejo.get().getSeller().getEmail().equals(userEmail)) {
            consejoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean updateAdvice(Long id, Consejo consejoDetalles, String userEmail, MultipartFile imageFile) throws IOException {
        Optional<Consejo> consejoOpcional = consejoRepository.findById(id);
        if (consejoOpcional.isPresent() && consejoOpcional.get().getSeller().getEmail().equals(userEmail)) {
            Consejo consejoExistente = consejoOpcional.get();
            consejoExistente.setTitle(consejoDetalles.getTitle());
            consejoExistente.setCategory(consejoDetalles.getCategory());
            consejoExistente.setPrice(consejoDetalles.getPrice());
            consejoExistente.setSecretText(consejoDetalles.getSecretText()); 

            if (!imageFile.isEmpty()) {
                consejoExistente.setImageBytes(imageFile.getBytes());
            }
            consejoRepository.save(consejoExistente);
            return true;
        }
        return false;
    }
}