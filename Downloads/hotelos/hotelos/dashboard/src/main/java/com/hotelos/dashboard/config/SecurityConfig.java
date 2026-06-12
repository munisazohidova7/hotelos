package com.hotelos.dashboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * HotelOS - Security Configuration (Task 3.2: Authentication requirement).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login.html", "/login", "/index.html",
                    "/css/**", "/js/**", "/ws/**", "/api/**",
                    "/*.html", "/webjars/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/index.html", true)
                .failureUrl("/login.html?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login.html")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var staff = User.withDefaultPasswordEncoder()
                .username("staff")
                .password("hotel123")
                .roles("STAFF")
                .build();
        var manager = User.withDefaultPasswordEncoder()
                .username("manager")
                .password("manager456")
                .roles("MANAGER", "STAFF")
                .build();
        return new InMemoryUserDetailsManager(staff, manager);
    }
}
