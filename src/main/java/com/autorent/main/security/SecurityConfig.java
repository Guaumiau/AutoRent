package com.autorent.main.security;

import com.autorent.main.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(customUserDetailsService);
        auth.setPasswordEncoder(NoOpPasswordEncoder.getInstance()); // sin encriptar
        return auth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(authProvider())

            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/login",
                            "/css/**",
                            "/js/**",
                            "/images/**",
                            "/webjars/**",
                            
                            "/login",
                            "/logout",
                            "/",
                            "/index",
                            "/reserva/catalogo"
                        ).permitAll()

                        .anyRequest().authenticated()
            )

            .formLogin(login -> login
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("username")   // debe coincidir con el nombre del input
                    .passwordParameter("password")
                    .defaultSuccessUrl("/", true)     // redirige al inicio
                    .failureUrl("/login?error=true")
                    .permitAll()
            )

            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .permitAll()
            );

        return http.build();
    }
}
