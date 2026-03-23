package es.codeurjc.web.security; 

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                // Añadimos las rutas de las imágenes de la BBDD y la vista de detalles
                .requestMatchers("/", "/registro", "/error", "/register", "/css/**", "/js/**", "/image/**", "/advice/*/image", "/advice-detail/**").permitAll()
                .anyRequest().authenticated()
        );

        http.formLogin(formLogin -> formLogin
                .loginPage("/login") 
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true) 
                .failureUrl("/login?error")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
        );

        return http.build();
    }
}