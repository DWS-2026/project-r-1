package es.codeurjc.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import es.codeurjc.web.model.Advice;
import es.codeurjc.web.model.User;
import es.codeurjc.web.repository.AdviceRepository;
import es.codeurjc.web.repository.UserRepository;

import java.io.InputStream;
import java.util.List;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdviceRepository adviceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        
        // We only insert data if the database is empty
        if (userRepository.count() == 0) {
            
            System.out.println("🛠️ Initializing sample data in the DB...");

            // 1. Create sample users
            User admin = new User("Admin", "admin@admin.com", passwordEncoder.encode("pass"));
            admin.setRoles(List.of("USER", "ADMIN"));
            userRepository.save(admin);

            User seller = new User("FerminAragones", "fermin@urjc.es", passwordEncoder.encode("pass"));
            userRepository.save(seller);

            User buyer = new User("Buyer", "comprador@urjc.es", passwordEncoder.encode("pass"));
            userRepository.save(buyer);

            // 2. Create sample advices
            Advice c1 = new Advice("How to find love", "Love", 5.00, "Beggars can't be choosers", seller);
            c1.setImageBytes(loadImage("static/image/rosa.jpg"));
            adviceRepository.save(c1);

            Advice c2 = new Advice("How to get rich", "Finance", 999.99, "Invest in Bitcoin", seller);
            c2.setImageBytes(loadImage("static/image/stonkss.jpg"));
            adviceRepository.save(c2);

            Advice c3 = new Advice("Pass DWS in 2026 😱 Real NO FAKE", "Studies", 240.00, "STUDY HARD!!!", admin);
            c3.setImageBytes(loadImage("static/image/nerd.png"));
            adviceRepository.save(c3);

            System.out.println("✅ Sample data successfully loaded.");
        }
    }

    // Helper method to read an image from the static folder and convert it to bytes for the DB
    private byte[] loadImage(String path) {
        try {
            InputStream is = new ClassPathResource(path).getInputStream();
            return is.readAllBytes();
        } catch (Exception e) {
            System.out.println("⚠️ Warning: Could not load the sample image: " + path);
            return null; // If it doesn't find the image, it stays null without breaking the app
        }
    }
}