package com.hospitalsr.config;

import com.hospitalsr.repositories.UsuarioSistemaRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioSistemaRepo usuarioRepo;

    public SecurityConfig(UsuarioSistemaRepo usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usuarioRepo.findByUsername(username)
                .map(u -> User.builder()
                        .username(u.getUsername())
                        .password(u.getPassword())
                        .roles(u.getRol())
                        .disabled(!Boolean.TRUE.equals(u.getActivo()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ── Recursos públicos ──────────────────────────────
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/uploads/**", "/login", "/login-process", "/")
                        .permitAll()

                        // ── ADMINISTRADOR: acceso total ────────────────────
                        .requestMatchers("/dashboard", "/usuarios/**", "/areas/**", "/universidades/**",
                                "/estudiantes/**")
                        .hasAnyRole("ADMINISTRADOR", "DIRECTOR")

                        // ── ADMINISTRADOR, DIRECTOR, MEDICO, DOCENTE ───────
                        .requestMatchers("/presencia/**", "/horarios/**", "/cronograma/**", "/reportes/**")
                        .hasAnyRole("ADMINISTRADOR", "DIRECTOR", "MEDICO", "DOCENTE")

                        // ── ESTUDIANTE ─────────────────────────────────────
                        .requestMatchers("/estudiante/**")
                        .hasRole("ESTUDIANTE")

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login-process")
                        .successHandler((req, res, auth) -> {
                            String role = auth.getAuthorities().stream()
                                    .map(a -> a.getAuthority())
                                    .findFirst().orElse("");
                            switch (role) {
                                case "ROLE_ADMINISTRADOR":
                                case "ROLE_DIRECTOR":
                                    res.sendRedirect("/dashboard");
                                    break;
                                case "ROLE_MEDICO":
                                case "ROLE_DOCENTE":
                                    res.sendRedirect("/presencia");
                                    break;
                                case "ROLE_ESTUDIANTE":
                                    res.sendRedirect("/estudiante/presencia");
                                    break;
                                default:
                                    res.sendRedirect("/login?error=true");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll());

        return http.build();
    }
}