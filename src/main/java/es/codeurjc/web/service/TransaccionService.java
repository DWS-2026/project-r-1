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
import es.codeurjc.web.dto.TransaccionMapper;

@Service
public class TransaccionService {
    
    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConsejoService consejoService;

    @Autowired
    private TransaccionMapper transaccionMapper;

    public TransaccionDTO toDTO(Transaccion t) {
        return transaccionMapper.toDTO(t);
    }

    public void save(Transaccion transaccion) {
        transaccionRepository.save(transaccion);
    }

    public List<Transaccion> findByBuyer(Usuario buyer) {
        return transaccionRepository.findByBuyer(buyer);
    }

    public Page<TransaccionDTO> findAll(Pageable pageable) {
        return transaccionRepository.findAll(pageable).map(transaccionMapper::toDTO);
    }

    // Cambiado de booleano a objeto para poder recuperar la ID y crear la cabecera Location en REST
    public Transaccion processPayment(Long consejoId, String buyerEmail) {
        Usuario buyer = usuarioService.findByEmail(buyerEmail).orElseThrow();
        Consejo consejo = consejoService.findById(consejoId).orElseThrow();

        if (consejo.getSeller() != null && consejo.getSeller().getId().equals(buyer.getId())) {
            return null; // El usuario no puede comprar su propio consejo
        }

        // FIX DoS/Logica: Impedir compras duplicadas que saturan la base de datos
        if (transaccionRepository.existsByBuyerAndConsejo(buyer, consejo)) {
            throw new IllegalArgumentException("Ya has adquirido este consejo. No es necesario comprarlo de nuevo.");
        }

        Transaccion transaccion = new Transaccion(buyer, consejo, consejo.getPrice());
        return transaccionRepository.save(transaccion);
    }
}