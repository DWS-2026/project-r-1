package es.codeurjc.web.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import es.codeurjc.web.model.User;
import es.codeurjc.web.repository.UserRepository;
import es.codeurjc.web.dto.UserDTO;
import es.codeurjc.web.dto.UserMapper;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    public UserDTO toDTO(User user) {
        return userMapper.toDTO(user);
    }

    public boolean registerNewUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try {
            userRepository.save(user);
            return true;
        } catch (DataIntegrityViolationException e) {
            // FIX Race Condition: If due to concurrency both threads pass the if, the DB will throw a uniqueness error.
            // We catch it so as not to return an unexpected 500.
            return false;
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Page<UserDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDTO);
    }

    public void updateProfile(String email, String name, String password, String confirmPassword) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setName(name);

        if (password != null && !password.isEmpty() && password.equals(confirmPassword)) {
            user.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(user);
    }

    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        userRepository.delete(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}