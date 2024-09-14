package com.masamonoke.userstarter.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Qualifier("jwtAuthFilter")
    private final OncePerRequestFilter filter;
    private final LogoutHandler logoutHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        req -> req.requestMatchers("/api/v1/auth/**", "/api/v1/profile/user/restore", "/api/v1/confirm/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                );
        httpSecurity.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.logout(
                l -> l.logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((req, resp, authentication) -> SecurityContextHolder.clearContext())
        );

        return httpSecurity.build();
    }
}
