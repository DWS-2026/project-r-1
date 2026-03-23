package es.codeurjc.web.service;

import es.codeurjc.web.model.Transaccion;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.TransaccionRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransaccionService {
    
    @Autowired
    private TransaccionRepository transaccionRepository;

    public void save(Transaccion transaccion) {
        transaccionRepository.save(transaccion);
    }
    public List<Transaccion> findByBuyer(Usuario buyer) {
        return transaccionRepository.findByBuyer(buyer);
    }
}