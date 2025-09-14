package com.sharkskin.store.config;

import com.sharkskin.store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(UserService userService, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userService::getUserByUsername;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/", 
                    "/products", 
                    "/product/detail/**", 
                    "/login", 
                    "/register", 
                    "/verify/**", 
                    "/css/**", 
                    "/js/**", 
                    "/images/**"
                ).permitAll()
                .requestMatchers("/cart/**", "/checkout/**", "/my_orders/**", "/admin/**").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(customAuthenticationSuccessHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll());
        return http.build();
    }
}
