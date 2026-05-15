package es.codeurjc.web.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList; // Added to clear collections
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import es.codeurjc.web.model.Advice;
import es.codeurjc.web.model.User;
import es.codeurjc.web.repository.AdviceRepository;
import es.codeurjc.web.dto.AdviceDTO;
import es.codeurjc.web.dto.AdviceMapper;

@Service
public class AdviceService {

    @Autowired
    private AdviceRepository adviceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AdviceMapper adviceMapper;

    private static final String UPLOADS_FOLDER = "uploads";

    public AdviceDTO toDTO(Advice advice) {
        return adviceMapper.toDTO(advice);
    }

    public void saveAdvice(Advice advice) {
        adviceRepository.save(advice);
    }

    public List<Advice> findAll() { 
        return adviceRepository.findAll(); 
    }

    public Page<AdviceDTO> findAll(Pageable pageable) {
        return adviceRepository.findAll(pageable).map(adviceMapper::toDTO);
    }

    public Optional<Advice> findById(Long id) {
        return adviceRepository.findById(id);
    }

    public List<Advice> findBySeller(User seller) {
        return adviceRepository.findBySeller(seller);
    }

    private String sanitizeHtml(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, Safelist.relaxed());
    }

    public Advice createAdvice(Advice advice, String sellerEmail, MultipartFile imageFile, MultipartFile attachmentFile) throws IOException {
        advice.setId(null); 
        
        // FIX Mass Assignment: We avoid injection of manipulated relational records.
        advice.setTransactions(new ArrayList<>());
        advice.setReviews(new ArrayList<>());
        
        // FIX Path Traversal / Mass Assignment
        advice.setAttachmentName(null);
        advice.setAttachmentPath(null);

        // FIX Business validation
        if (advice.getPrice() < 0) {
            throw new IllegalArgumentException("The price cannot be negative.");
        }

        advice.setSecretText(sanitizeHtml(advice.getSecretText()));
        
        User seller = userService.findByEmail(sellerEmail).orElseThrow();
        advice.setSeller(seller);
        
        if (imageFile != null && !imageFile.isEmpty()) {
            advice.setImageBytes(imageFile.getBytes());
        }
        
        handleAttachment(advice, attachmentFile);
        
        return adviceRepository.save(advice);
    }

    public boolean deleteAdvice(Long id, String userEmail) {
        Optional<Advice> adviceOpt = adviceRepository.findById(id);
        if (adviceOpt.isEmpty()) {
            return false;
        }

        Advice advice = adviceOpt.get();
        User user = userService.findByEmail(userEmail).orElseThrow();

        boolean isOwner = advice.getSeller().getEmail().equals(userEmail);
        boolean isAdmin = user.getRoles().contains("ADMIN");

        if (isOwner || isAdmin) {
            adviceRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Advice> updateAdvice(Long id, Advice adviceDetails, String userEmail, MultipartFile imageFile, MultipartFile attachmentFile) throws IOException {
        Optional<Advice> optionalAdvice = adviceRepository.findById(id);
        if (optionalAdvice.isEmpty()) {
            return Optional.empty();
        }

        Advice existingAdvice = optionalAdvice.get();
        User user = userService.findByEmail(userEmail).orElseThrow();

        boolean isOwner = existingAdvice.getSeller().getEmail().equals(userEmail);
        boolean isAdmin = user.getRoles().contains("ADMIN");

        if (!isOwner && !isAdmin) {
            return Optional.empty();
        }

        // FIX Business validation on update
        if (adviceDetails.getPrice() < 0) {
            throw new IllegalArgumentException("The price cannot be negative.");
        }

        existingAdvice.setTitle(adviceDetails.getTitle());
        existingAdvice.setCategory(adviceDetails.getCategory());
        existingAdvice.setPrice(adviceDetails.getPrice());

        existingAdvice.setSecretText(sanitizeHtml(adviceDetails.getSecretText()));

        if (imageFile != null && !imageFile.isEmpty()) {
            existingAdvice.setImageBytes(imageFile.getBytes());
        }
        
        handleAttachment(existingAdvice, attachmentFile);
        
        return Optional.of(adviceRepository.save(existingAdvice));
    }

    private void handleAttachment(Advice advice, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            Path folderPath = Paths.get(UPLOADS_FOLDER).toAbsolutePath();
            Files.createDirectories(folderPath); 
            
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.contains("..")) {
                throw new SecurityException("Path Traversal attempt detected in upload");
            }
            
            String sanitizedName = originalName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
            String uniqueName = UUID.randomUUID().toString() + "_" + sanitizedName;
            
            Path filePath = folderPath.resolve(uniqueName);
            file.transferTo(filePath.toFile());
            
            advice.setAttachmentName(originalName);
            advice.setAttachmentPath(filePath.toString());
        }
    }

    public Resource getAttachmentResource(Long id) {
        try {
            Advice advice = adviceRepository.findById(id).orElseThrow();
            if (advice.getAttachmentPath() != null) {
                Path basePath = Paths.get(UPLOADS_FOLDER).toAbsolutePath().normalize();
                Path filePath = Paths.get(advice.getAttachmentPath()).toAbsolutePath().normalize();
                
                if (!filePath.startsWith(basePath)) {
                    throw new SecurityException("Attempted to read files outside the allowed directory");
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