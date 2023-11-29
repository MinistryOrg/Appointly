package com.mom.appointly.auth.config;

import com.mom.appointly.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityFilterChainConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(req ->
                        req.requestMatchers("/api/v1/auth/appointly/**")
                                .permitAll()
                                .requestMatchers("/api/v1/appointly/admin/**")
                                .hasAnyAuthority(String.valueOf(Role.ADMIN)) // only the user with this perm have the authority
                                .requestMatchers("/api/v1/appointly/user/**")
                                .hasAnyAuthority(String.valueOf(Role.USER), String.valueOf(Role.ADMIN))
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS)) // because every request should be authenticated
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // we check everything
                .logout(logout ->
                        logout.logoutUrl("/api/v1/auth/logout")
                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }

}