package es.codeurjc.web.service;

import es.codeurjc.web.model.Transaccion;
import es.codeurjc.web.repository.TransaccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransaccionService {
    
    @Autowired
    private TransaccionRepository transaccionRepository;

    public void save(Transaccion transaccion) {
        transaccionRepository.save(transaccion);
    }
}