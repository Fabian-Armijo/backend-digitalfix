package com.digitalfix.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Habilitamos CORS indicándole que use el Bean que crearemos abajo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Deshabilitamos CSRF porque nuestra comunicación será puramente vía API REST usando JWT
                .csrf(csrf -> csrf.disable())

                // Configuramos las reglas de autorización por rutas y roles
                .authorizeHttpRequests(auth -> auth

                        // 1. Reglas para el Microservicio de Órdenes de Trabajo (WorkOrders)
                        // Solo Clientes y Supervisores pueden crear órdenes
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/workorders").hasAnyRole("CLIENTE", "SUPERVISOR")
                        // Solo Supervisores pueden asignar técnicos y hacer transiciones de estado
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/workorders/**").hasRole("SUPERVISOR")
                        // Cualquiera autenticado puede ver las órdenes (luego podrías filtrar por ID en el servicio)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/workorders/**").authenticated()

                        // 2. Reglas para el Microservicio de Catálogo (Repuestos y Servicios)
                        // Solo el Admin puede crear o modificar servicios técnicos y repuestos
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/catalog/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/catalog/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/catalog/**").hasRole("ADMIN")
                        // El Supervisor puede descontar stock
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/catalog/spare-parts/*/reduce-stock").hasRole("SUPERVISOR")
                        // Todos (Cliente, Supervisor, Admin) pueden ver el catálogo de servicios
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/catalog/**").authenticated()

                        // 3. Reglas para el Microservicio de Técnicos
                        // Solo Admin puede crear, actualizar y eliminar técnicos
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/technicians/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/technicians/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/technicians/**").hasRole("ADMIN")
                        // Admin y Supervisor pueden consultar técnicos y cambiar su estado
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/technicians/**").hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/technicians/**").hasAnyRole("ADMIN", "SUPERVISOR")

                        // Cualquier otra petición debe estar autenticada
                        .anyRequest().authenticated()
                )
                // 3. Validación de Resource Server para JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Autorizamos explícitamente el origen exacto de tu frontend en Angular
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

        // Permitimos los métodos HTTP que consumirá tu API REST (CRUD)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));

        // Es vital permitir la cabecera Authorization para que pase el token JWT[cite: 1]
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Si más adelante requieres enviar cookies de sesión cruzadas
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicamos esta configuración a todas las rutas de la API del BFF
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // Este Bean se encarga de extraer los roles del JWT emitido por Microsoft Entra ID
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new AzureAdRoleConverter());
        return converter;
    }

    // Clase interna para mapear los roles de Azure AD al formato de Spring Security
    static class AzureAdRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Azure AD suele enviar los roles en el claim "roles"
            List<String> roles = jwt.getClaimAsStringList("roles");

            if (roles == null || roles.isEmpty()) {
                return Collections.emptyList();
            }

            // Convertimos los roles de Azure (ej: "Admin") a GrantedAuthority de Spring (ej: "ROLE_ADMIN")
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
        }
    }
}
