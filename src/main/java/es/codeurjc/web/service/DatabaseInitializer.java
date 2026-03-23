package es.codeurjc.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.ConsejoRepository;
import es.codeurjc.web.repository.UsuarioRepository;

import java.io.InputStream;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConsejoRepository consejoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        
        // Solo insertamos datos si la base de datos está vacía
        if (usuarioRepository.count() == 0) {
            
            System.out.println("🛠️ Inicializando datos de ejemplo en la BBDD...");

            // 1. Crear usuarios de ejemplo
            Usuario admin = new Usuario("Admin", "admin@admin.com", passwordEncoder.encode("pass"));
            admin.setRoles(List.of("USER", "ADMIN"));
            usuarioRepository.save(admin);

            Usuario seller = new Usuario("FerminAragones", "fermin@urjc.es", passwordEncoder.encode("pass"));
            usuarioRepository.save(seller);

            Usuario buyer = new Usuario("Comprador", "comprador@urjc.es", passwordEncoder.encode("pass"));
            usuarioRepository.save(buyer);

            // 2. Crear consejos de ejemplo
            Consejo c1 = new Consejo("Cómo encontrar el amor", "Amor", 5.00, "El secreto es ser tú mismo y lavarte los dientes.", seller);
            c1.setImageBytes(cargarImagen("static/image/rosa.jpg"));
            consejoRepository.save(c1);

            Consejo c2 = new Consejo("Cómo hacerte rico", "Finanzas", 999.99, "Compra barato, vende caro. De nada.", seller);
            c2.setImageBytes(cargarImagen("static/image/stonkss.jpg"));
            consejoRepository.save(c2);

            Consejo c3 = new Consejo("Aprobar DWS en 2026 😱 Real NO FAKE", "Estudios", 240.00, "Estudia las diapositivas y reza a Spring Boot.", admin);
            c3.setImageBytes(cargarImagen("static/image/nerd.png"));
            consejoRepository.save(c3);

            System.out.println("✅ Datos de ejemplo cargados con éxito.");
        }
    }

    // Método auxiliar para leer una imagen de la carpeta static y convertirla a bytes para la BBDD
    private byte[] cargarImagen(String ruta) {
        try {
            InputStream is = new ClassPathResource(ruta).getInputStream();
            return is.readAllBytes();
        } catch (Exception e) {
            System.out.println("⚠️ Aviso: No se pudo cargar la imagen de ejemplo: " + ruta);
            return null; // Si no encuentra la imagen, se queda a null sin romper la app
        }
    }
}