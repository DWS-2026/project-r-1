package es.codeurjc.web.security.jwt;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {
    @NotBlank(message = "The name is mandatory")
    @Size(max = 100, message = "The name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "The email is mandatory")
    @Email(message = "The email must have a valid format")
    @Size(max = 255, message = "The email cannot exceed 255 characters")
    private String email;

    @NotBlank(message = "The password is mandatory")
    @Size(min = 4, max = 100, message = "The password must be between 4 and 100 characters")
    private String password;

    public SignupRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}