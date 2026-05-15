package es.codeurjc.web.restcontroller;

import java.security.Principal;
import java.util.Optional;
import java.util.NoSuchElementException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import es.codeurjc.web.dto.UserDTO;
import es.codeurjc.web.dto.UserUpdateDTO;
import es.codeurjc.web.model.User;
import es.codeurjc.web.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("The user with ID " + id + " does not exist."));
        return ResponseEntity.ok(userService.toDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO updatedData, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        User userInDB = userService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("The user to modify does not exist."));
        
        if (userInDB.getEmail().equals(principal.getName())) {
            userService.updateProfile(principal.getName(), updatedData.name(), updatedData.password(), updatedData.confirmPassword());
            
            User updated = userService.findByEmail(principal.getName()).get();
            return ResponseEntity.ok(userService.toDTO(updated));
        } else {
            throw new NoSuchElementException("You do not have permission to edit this user.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Principal principal, HttpServletRequest request) {
        if (principal == null) return ResponseEntity.status(401).build();

        User userInDB = userService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("The user to delete does not exist."));
        
        boolean isOwner = userInDB.getEmail().equals(principal.getName());
        boolean isAdmin = request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ADMIN");

        if (isOwner || isAdmin) {
            userService.deleteUserById(id);
            return ResponseEntity.noContent().build();
        } else {
            throw new NoSuchElementException("You do not have permission to delete this account.");
        }
    }
}