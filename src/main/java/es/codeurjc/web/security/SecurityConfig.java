package es.codeurjc.web.security; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private RestUnauthorizedHandler restUnauthorizedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- FILTRO 1: API REST ---
    @Bean
    @Order(1) // Este filtro tiene prioridad. Captura todo lo que empiece por /api/
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        
        http.securityMatcher("/api/v1/**"); 
        
        // Usamos nuestro handler para que devuelva JSON en caso de error
        http.exceptionHandling(handling -> handling.authenticationEntryPoint(restUnauthorizedHandler));
        
        http.authorizeHttpRequests(authorize -> authorize
                // Los endpoints públicos de la API
                .requestMatchers(HttpMethod.GET, "/api/v1/consejos/**", "/api/v1/valoraciones/**").permitAll()
                // Solo el admin puede listar todos los usuarios desde la API
                .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/").hasRole("ADMIN") 
                // El resto de llamadas REST requieren autenticación
                .anyRequest().authenticated()
        );

        // Desactivamos el login por formulario en la API
        http.formLogin(formLogin -> formLogin.disable());
        // Desactivamos CSRF solo en la API, como pide la rúbrica
        http.csrf(csrf -> csrf.disable());
        // Habilitamos Basic Auth para poder hacer pruebas desde Postman
        http.httpBasic(Customizer.withDefaults());
        // La API no usa sesiones
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // --- FILTRO 2: WEB ---
    @Bean
    @Order(2) // Este filtro actúa como respaldo para el resto de rutas (la web normal)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                // Añadimos las rutas de las imágenes de la BBDD y la vista de detalles
                .requestMatchers("/", "/registro", "/error", "/register", "/css/**", "/js/**", "/image/**", "/advice/*/image", "/advice-detail/**").permitAll()
                // Zona exclusiva para el Administrador
                .requestMatchers("/admin/**").hasRole("ADMIN") 
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