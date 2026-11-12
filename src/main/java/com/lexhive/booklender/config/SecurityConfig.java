package com.lexhive.booklender.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableConfigurationProperties({SecurityUsersProperties.class, BorrowingProperties.class})
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String LIBRARIAN = "LIBRARIAN";
    private static final String MEMBER = "MEMBER";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console", "/h2-console/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books", "/books/**", "/members", "/members/**", "/loans", "/loans/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/loans", "/loans/*/return").hasAnyRole(ADMIN, LIBRARIAN, MEMBER)
                        .requestMatchers(HttpMethod.POST, "/books", "/members").hasAnyRole(ADMIN, LIBRARIAN)
                        .requestMatchers(HttpMethod.PUT, "/books/**", "/members/**").hasAnyRole(ADMIN, LIBRARIAN)
                        .requestMatchers(HttpMethod.DELETE, "/books/**", "/members/**").hasRole(ADMIN)
                        .requestMatchers("/actuator/**").authenticated()
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            // Lets browsers show their username/password prompt for direct GET requests.
                            response.setHeader("WWW-Authenticate", "Basic realm=\"BookLender\"");
                            write(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
                        })
                        .accessDeniedHandler((request, response, exception) -> write(response, HttpServletResponse.SC_FORBIDDEN, "You do not have permission to perform this action")))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(SecurityUsersProperties properties, PasswordEncoder passwordEncoder) {
        if (properties.getUsers() == null || properties.getUsers().isEmpty()) {
            throw new IllegalStateException("At least one API user must be configured in booklender.security.users");
        }
        List<UserDetails> users = properties.getUsers().stream()
                .map(account -> user(account, passwordEncoder))
                .toList();
        return new InMemoryUserDetailsManager(users);
    }

    private static UserDetails user(SecurityUsersProperties.UserAccount account, PasswordEncoder passwordEncoder) {
        if (isBlank(account.getUsername()) || isBlank(account.getPassword())
                || account.getRoles() == null || account.getRoles().isEmpty()) {
            throw new IllegalStateException("Each API user needs a username, password, and at least one role");
        }
        return User.withUsername(account.getUsername().trim())
                .password(passwordEncoder.encode(account.getPassword()))
                .roles(account.getRoles().toArray(String[]::new))
                .build();
    }

    private static void write(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
