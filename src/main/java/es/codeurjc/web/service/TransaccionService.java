package es.codeurjc.web.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Transaccion;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.TransaccionRepository;
import es.codeurjc.web.dto.TransaccionDTO;

@Service
public class TransaccionService {
    
    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConsejoService consejoService;

    // --- Métodos de conversión a DTO ---
    public TransaccionDTO toDTO(Transaccion t) {
        return new TransaccionDTO(
                t.getId(),
                t.getBuyer() != null ? t.getBuyer().getId() : null,
                t.getBuyer() != null ? t.getBuyer().getNombre() : "Unknown",
                t.getConsejo() != null ? t.getConsejo().getId() : null,
                t.getConsejo() != null ? t.getConsejo().getTitle() : "Unknown",
                t.getPriceAtPurchase(),
                t.getPurchaseDate()
        );
    }

    public void save(Transaccion transaccion) {
        transaccionRepository.save(transaccion);
    }

    public List<Transaccion> findByBuyer(Usuario buyer) {
        return transaccionRepository.findByBuyer(buyer);
    }

    // Nuevo método para soportar paginación en la API REST
    public Page<TransaccionDTO> findAll(Pageable pageable) {
        return transaccionRepository.findAll(pageable).map(this::toDTO);
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