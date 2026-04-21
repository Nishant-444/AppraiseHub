package dev.nishants.appraisal.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import dev.nishants.appraisal.security.JwtAuthFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Public — login only
            .requestMatchers("/api/auth/**").permitAll()

            // HR only
            .requestMatchers(HttpMethod.POST, "/api/users").hasRole("HR")
            .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("HR")
            .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("HR")
            .requestMatchers(HttpMethod.POST, "/api/departments/**").hasRole("HR")
            .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasRole("HR")
            .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasRole("HR")
            .requestMatchers(HttpMethod.POST, "/api/appraisals").hasRole("HR")
            .requestMatchers(HttpMethod.POST, "/api/appraisals/cycle/**").hasRole("HR")
            .requestMatchers(HttpMethod.PATCH, "/api/appraisals/*/approve").hasRole("HR")
            .requestMatchers("/api/reports/cycle/**").hasRole("HR")

            // Manager only
            .requestMatchers(HttpMethod.POST, "/api/goals").hasRole("MANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/goals/**").hasRole("MANAGER")
            .requestMatchers(HttpMethod.DELETE, "/api/goals/**").hasRole("MANAGER")
            .requestMatchers(HttpMethod.PUT, "/api/appraisals/*/manager-review/**").hasRole("MANAGER")
            .requestMatchers("/api/reports/manager/**").hasAnyRole("MANAGER", "HR")

            // Employee + Manager can submit self-assessment (managers can be appraised too)
            .requestMatchers(HttpMethod.PUT, "/api/appraisals/*/self-assessment/**").hasAnyRole("EMPLOYEE", "MANAGER")
            .requestMatchers(HttpMethod.PATCH, "/api/appraisals/*/acknowledge").hasAnyRole("EMPLOYEE", "MANAGER")
            .requestMatchers(HttpMethod.PATCH, "/api/goals/*/progress").hasRole("EMPLOYEE")
            .requestMatchers("/api/reports/employee/**").hasAnyRole("EMPLOYEE", "HR")

            // Authenticated — any role
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // 1. EXACT matches only. No wildcards.
    config.setAllowedOrigins(List.of(
        "http://localhost:5173",
        "http://localhost:3000",
        "https://appraise-hub.vercel.app"));

    // 2. Allow all standard methods
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    // 3. Allow all incoming headers
    config.setAllowedHeaders(List.of("*"));

    // 4. Expose the Authorization header to the frontend
    config.setExposedHeaders(List.of("Authorization"));

    // 5. Allow credentials (cookies/auth headers)
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}