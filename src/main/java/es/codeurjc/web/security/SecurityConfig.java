package es.codeurjc.web.security; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import es.codeurjc.web.security.jwt.JwtRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private RestUnauthorizedHandler restUnauthorizedHandler;

    @Autowired
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    @Order(1) 
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        
        http.securityMatcher("/api/v1/**"); 
        
        http.exceptionHandling(handling -> handling
                .authenticationEntryPoint(restUnauthorizedHandler)
                .accessDeniedHandler(restAccessDeniedHandler)
        );
        
        http.authorizeHttpRequests(authorize -> authorize
                // Endpoints públicos de autenticación
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/signup").permitAll()

                // Lectura pública del catálogo de consejos e imágenes
                .requestMatchers(HttpMethod.GET, "/api/v1/consejos/**").permitAll()

                // FIX IDOR: La lectura de cualquier valoración es pública (listado y detalle)
                .requestMatchers(HttpMethod.GET, "/api/v1/valoraciones/").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/valoraciones/{id}").permitAll()

                // FIX IDOR: Solo el admin puede consultar datos de otros usuarios por ID o listarlos
                .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/**").hasRole("ADMIN")

                // FIX FUGA DE DATOS: Solo el admin puede ver el listado global de transacciones
                // El endpoint /me sigue accesible para usuarios autenticados gracias a anyRequest().authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/transacciones/").hasRole("ADMIN")

                .anyRequest().authenticated()
        );

        http.formLogin(formLogin -> formLogin.disable());
        http.csrf(csrf -> csrf.disable());
        http.httpBasic(httpBasic -> httpBasic.disable()); 
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/registro", "/error", "/register", "/css/**", "/js/**", "/image/**", "/advice/*/image", "/advice-detail/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
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