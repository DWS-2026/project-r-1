package es.codeurjc.web.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Transaccion;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.TransaccionRepository;

@Service
public class TransaccionService {
    
    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConsejoService consejoService;

    public void save(Transaccion transaccion) {
        transaccionRepository.save(transaccion);
    }

    public List<Transaccion> findByBuyer(Usuario buyer) {
        return transaccionRepository.findByBuyer(buyer);
    }

    public boolean processPayment(Long consejoId, String buyerEmail) {
        Usuario buyer = usuarioService.findByEmail(buyerEmail).orElseThrow();
        Consejo consejo = consejoService.findById(consejoId).orElseThrow();

        if (consejo.getSeller() != null && consejo.getSeller().getId().equals(buyer.getId())) {
            return false; // El usuario no puede comprar su propio consejo
        }

        Transaccion transaccion = new Transaccion(buyer, consejo, consejo.getPrice());
        transaccionRepository.save(transaccion);
        return true;
    }
}