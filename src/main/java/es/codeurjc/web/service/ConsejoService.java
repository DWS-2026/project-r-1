package es.codeurjc.web.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList; // Añadido para limpiar colecciones
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
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
import es.codeurjc.web.dto.ConsejoMapper;

@Service
public class ConsejoService {

    @Autowired
    private ConsejoRepository consejoRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ConsejoMapper consejoMapper;

    private static final String UPLOADS_FOLDER = "uploads";

    public ConsejoDTO toDTO(Consejo consejo) {
        return consejoMapper.toDTO(consejo);
    }

    public void saveConsejo(Consejo consejo) {
        consejoRepository.save(consejo);
    }

    public List<Consejo> findAll() { 
        return consejoRepository.findAll(); 
    }

    public Page<ConsejoDTO> findAll(Pageable pageable) {
        return consejoRepository.findAll(pageable).map(consejoMapper::toDTO);
    }

    public Optional<Consejo> findById(Long id) {
        return consejoRepository.findById(id);
    }

    public List<Consejo> findBySeller(Usuario seller) {
        return consejoRepository.findBySeller(seller);
    }

    private String sanitizeHtml(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, Safelist.relaxed());
    }

    public Consejo createAdvice(Consejo consejo, String sellerEmail, MultipartFile imageFile, MultipartFile attachmentFile) throws IOException {
        consejo.setId(null); 
        
        // FIX Mass Assignment: Evitamos inyección de registros relacionales manipulados.
        consejo.setTransactions(new ArrayList<>());
        consejo.setReviews(new ArrayList<>());
        
        // FIX Path Traversal / Mass Assignment
        consejo.setAttachmentName(null);
        consejo.setAttachmentPath(null);

        // FIX Validación de negocio
        if (consejo.getPrice() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }

        consejo.setSecretText(sanitizeHtml(consejo.getSecretText()));
        
        Usuario seller = usuarioService.findByEmail(sellerEmail).orElseThrow();
        consejo.setSeller(seller);
        
        if (imageFile != null && !imageFile.isEmpty()) {
            consejo.setImageBytes(imageFile.getBytes());
        }
        
        handleAttachment(consejo, attachmentFile);
        
        return consejoRepository.save(consejo);
    }

    public boolean deleteAdvice(Long id, String userEmail) {
        Optional<Consejo> consejoOpt = consejoRepository.findById(id);
        if (consejoOpt.isEmpty()) {
            return false;
        }

        Consejo consejo = consejoOpt.get();
        Usuario user = usuarioService.findByEmail(userEmail).orElseThrow();

        boolean isOwner = consejo.getSeller().getEmail().equals(userEmail);
        boolean isAdmin = user.getRoles().contains("ADMIN");

        if (isOwner || isAdmin) {
            consejoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Consejo> updateAdvice(Long id, Consejo consejoDetalles, String userEmail, MultipartFile imageFile, MultipartFile attachmentFile) throws IOException {
        Optional<Consejo> consejoOpcional = consejoRepository.findById(id);
        if (consejoOpcional.isEmpty()) {
            return Optional.empty();
        }

        Consejo consejoExistente = consejoOpcional.get();
        Usuario user = usuarioService.findByEmail(userEmail).orElseThrow();

        boolean isOwner = consejoExistente.getSeller().getEmail().equals(userEmail);
        boolean isAdmin = user.getRoles().contains("ADMIN");

        if (!isOwner && !isAdmin) {
            return Optional.empty();
        }

        // FIX Validación de negocio en la actualización
        if (consejoDetalles.getPrice() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }

        consejoExistente.setTitle(consejoDetalles.getTitle());
        consejoExistente.setCategory(consejoDetalles.getCategory());
        consejoExistente.setPrice(consejoDetalles.getPrice());

        consejoExistente.setSecretText(sanitizeHtml(consejoDetalles.getSecretText()));

        if (imageFile != null && !imageFile.isEmpty()) {
            consejoExistente.setImageBytes(imageFile.getBytes());
        }
        
        handleAttachment(consejoExistente, attachmentFile);
        
        return Optional.of(consejoRepository.save(consejoExistente));
    }

    private void handleAttachment(Consejo consejo, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            Path folderPath = Paths.get(UPLOADS_FOLDER).toAbsolutePath();
            Files.createDirectories(folderPath); 
            
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.contains("..")) {
                throw new SecurityException("Intento de Path Traversal detectado en la subida");
            }
            
            String sanitizedName = originalName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            String uniqueName = UUID.randomUUID().toString() + "_" + sanitizedName;
            
            Path filePath = folderPath.resolve(uniqueName);
            file.transferTo(filePath.toFile());
            
            consejo.setAttachmentName(originalName);
            consejo.setAttachmentPath(filePath.toString());
        }
    }

    public Resource getAttachmentResource(Long id) {
        try {
            Consejo consejo = consejoRepository.findById(id).orElseThrow();
            if (consejo.getAttachmentPath() != null) {
                Path basePath = Paths.get(UPLOADS_FOLDER).toAbsolutePath().normalize();
                Path filePath = Paths.get(consejo.getAttachmentPath()).toAbsolutePath().normalize();
                
                if (!filePath.startsWith(basePath)) {
                    throw new SecurityException("Intento de lectura de archivos fuera del directorio permitido");
                }

                Resource resource = new UrlResource(filePath.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return resource;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}