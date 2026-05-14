package com.nazarukiv.scrapepilotai.config;

import com.nazarukiv.scrapepilotai.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(DefaultAdminProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/css/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/access-denied").authenticated()
                        .requestMatchers(HttpMethod.POST, "/dashboard/tasks/*/execute").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/tasks/*/execute", "/tasks/*/status").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/api/tasks", "/api/tasks/*/execute", "/api/scrape/**")
                        .hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/tasks/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers("/actuator/**", "/management/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers("/", "/dashboard", "/tasks/**", "/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", false)
                        .failureHandler(this::handleAuthenticationFailure)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/access-denied")
                )
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?expired")
                        .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void handleAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            Exception exception
    ) throws java.io.IOException {
        String failure = exception instanceof DisabledException ? "disabled" : "error";
        response.sendRedirect(request.getContextPath() + "/login?" + failure);
    }
}
