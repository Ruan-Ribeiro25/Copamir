package com.vidaplus.config;

import com.vidaplus.service.impl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // --- CORREÇÃO AQUI: Adicionado manifest.json e sw.js na lista ---
                .requestMatchers(
                    "/css/**", 
                    "/js/**", 
                    "/uploads/**", 
                    "/img/**", 
                    "/webjars/**", 
                    "/fragments/**",
                    "/manifest.json",  // <--- OBRIGATÓRIO PARA O PWA
                    "/sw.js"           // <--- OBRIGATÓRIO PARA O PWA
                ).permitAll()
                
                // --- MONITORAMENTO ---
                .requestMatchers("/actuator/**").permitAll()

                // Páginas públicas
                .requestMatchers(
                    "/", "/home", "/login", "/login-professional", "/admin/login",
                    "/register", "/register-professional", "/register-medico", "/register-admin",
                    "/verificar-conta", "/forgot-password", "/enter-code", 
                    "/verify-reset-code", "/update-password", "/perfil/selecionar-polo",
                    "/acesso-profissional", "/bem-vindo"
                ).permitAll()
                
                // --- ROTAS PROTEGIDAS ---
                .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .requestMatchers("/financeiro/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                .requestMatchers("/profissional/**").hasAnyAuthority(
                    "MEDICO", "ROLE_MEDICO", 
                    "ENFERMEIRO", "ROLE_ENFERMEIRO", 
                    "MOTORISTA", "ROLE_MOTORISTA", 
                    "ADMIN", "ROLE_ADMIN"
                )
                .requestMatchers("/pacientes/**", "/agendamentos/**", "/documentos/**", "/telemedicina/**").authenticated()
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getRequestURI().startsWith("/admin")) {
                        response.sendRedirect("/admin/login?error=denied");
                    } else {
                        response.sendRedirect("/home");
                    }
                })
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}