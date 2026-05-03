package es.codeurjc.web.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import es.codeurjc.web.model.Consejo;
import es.codeurjc.web.model.Usuario;
import es.codeurjc.web.repository.ConsejoRepository;
import es.codeurjc.web.dto.ConsejoDTO;

@Service
public class ConsejoService {

    @Autowired
    private ConsejoRepository consejoRepository;

    @Autowired
    private UsuarioService usuarioService;

    // Carpeta donde guardaremos físicamente los archivos
    private static final String UPLOADS_FOLDER = "uploads";

    // --- Métodos de conversión a DTO ---
    public ConsejoDTO toDTO(Consejo consejo) {
        return new ConsejoDTO(
                consejo.getId(),
                consejo.getTitle(),
                consejo.getCategory(),
                consejo.getPrice(),
                consejo.getSecretText(),
                consejo.getAttachmentName(),
                consejo.getSeller() != null ? consejo.getSeller().getId() : null,
                consejo.getSeller() != null ? consejo.getSeller().getNombre() : "Usuario Anónimo"
        );
    }

    public void saveConsejo(Consejo consejo) {
        consejoRepository.save(consejo);
    }

    public List<Consejo> findAll() { 
        return consejoRepository.findAll(); 
    }

    // Nuevo método para soportar paginación en la API REST
    public Page<ConsejoDTO> findAll(Pageable pageable) {
        return consejoRepository.findAll(pageable).map(this::toDTO);
    }

    public Optional<Consejo> findById(Long id) {
        return consejoRepository.findById(id);
    }

    public List<Consejo> findBySeller(Usuario seller) {
        return consejoRepository.findBySeller(seller);
    }

    public Consejo createAdvice(Consejo consejo, String sellerEmail, MultipartFile imageFile, MultipartFile attachmentFile) throws IOException {
        Usuario seller = usuarioService.findByEmail(sellerEmail).orElseThrow();
        consejo.setSeller(seller);
        
        if (imageFile != null && !imageFile.isEmpty()) {
            consejo.setImageBytes(imageFile.getBytes());
        }
        
        handleAttachment(consejo, attachmentFile);
        
        return consejoRepository.save(consejo);
    }

    public boolean deleteAdvice(Long id, String userEmail) {
        Optional<Consejo> consejo = consejoRepository.findById(id);
        // IMPORTANTE: En el futuro permitiremos al ADMIN saltarse esta comprobación
        if (consejo.isPresent() && consejo.get().getSeller().getEmail().equals(userEmail)) {
            consejoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Consejo> updateAdvice(Long id, Consejo consejoDetalles, String userEmail, MultipartFile imageFile, MultipartFile attachmentFile) throws IOException {
        Optional<Consejo> consejoOpcional = consejoRepository.findById(id);
        if (consejoOpcional.isPresent() && consejoOpcional.get().getSeller().getEmail().equals(userEmail)) {
            Consejo consejoExistente = consejoOpcional.get();
            consejoExistente.setTitle(consejoDetalles.getTitle());
            consejoExistente.setCategory(consejoDetalles.getCategory());
            consejoExistente.setPrice(consejoDetalles.getPrice());
            consejoExistente.setSecretText(consejoDetalles.getSecretText()); 

            if (imageFile != null && !imageFile.isEmpty()) {
                consejoExistente.setImageBytes(imageFile.getBytes());
            }
            
            handleAttachment(consejoExistente, attachmentFile);
            
            return Optional.of(consejoRepository.save(consejoExistente));
        }
        return Optional.empty();
    }

    // --- LÓGICA DE ALMACENAMIENTO EN DISCO ---
    private void handleAttachment(Consejo consejo, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            // Forzamos a que la ruta sea absoluta respecto a la raíz del proyecto
            Path folderPath = Paths.get(UPLOADS_FOLDER).toAbsolutePath();
            Files.createDirectories(folderPath); // Crea la carpeta si no existe
            
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.contains("..")) {
                // Contramedida: Evita el Path Traversal
                throw new SecurityException("Intento de Path Traversal detectado");
            }
            
            // Saneamos el nombre original y le ponemos un UUID para que sea único
            String sanitizedName = originalName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            String uniqueName = UUID.randomUUID().toString() + "_" + sanitizedName;
            
            Path filePath = folderPath.resolve(uniqueName);
            file.transferTo(filePath.toFile());
            
            // Guardamos el nombre original para el usuario y la ruta interna para nosotros
            consejo.setAttachmentName(originalName);
            consejo.setAttachmentPath(filePath.toString());
        }
    }

    // --- LÓGICA DE DESCARGA DESDE DISCO ---
    public Resource getAttachmentResource(Long id) {
        try {
            Consejo consejo = consejoRepository.findById(id).orElseThrow();
            if (consejo.getAttachmentPath() != null) {
                // Resolvemos la ruta absoluta para leer el fichero
                Path path = Paths.get(consejo.getAttachmentPath()).toAbsolutePath();
                Resource resource = new UrlResource(path.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return resource;
                }
            }
        } catch (Exception e) {
            // Log del error omitido por simplicidad
        }
        return null;
    }
}