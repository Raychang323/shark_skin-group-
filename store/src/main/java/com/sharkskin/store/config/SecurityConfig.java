package com.sharkskin.store.config;

import com.sharkskin.store.service.UserService; // Keep import for other uses if any, but not for constructor
import com.sharkskin.store.repositories.UserRepository; // Import UserRepository
import com.sharkskin.store.model.UserModel; // Import UserModel
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import UsernameNotFoundException
import org.springframework.security.core.userdetails.User; // Import Spring Security User
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import SimpleGrantedAuthority
import java.util.Collections; // Import Collections
import org.springframework.core.annotation.Order; // Import Order


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // private final UserService userService; // Removed from here
    private final UserRepository userRepository; // Inject UserRepository directly
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    public SecurityConfig(UserRepository userRepository, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) { // Modified constructor
        this.userRepository = userRepository;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Directly implement UserDetailsService logic here using UserRepository
        return username -> {
            UserModel userModel = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

            return new User(
                userModel.getUsername(),
                userModel.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userModel.getRole()))
            );
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Admin Security Filter Chain
    @Bean
    @Order(1) // Process this chain first
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**", "/portal/**") // Apply this chain to admin and portal paths
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/admin/login", "/admin/logout").permitAll() // Admin login/logout are public
                .anyRequest().hasRole("ADMIN") // All other admin/portal paths require ADMIN role
            )
            .formLogin(form -> form
                .loginPage("/admin/login") // Admin login page
                .loginProcessingUrl("/admin/login") // Process admin login POST here
                .failureUrl("/admin/login?error") // Redirect to admin login on failure
                .successHandler(customAuthenticationSuccessHandler) // Use custom handler
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout") // Admin logout URL
                .logoutSuccessUrl("/admin/login") // Redirect to admin login after logout
                .permitAll()
            );
        return http.build();
    }

    // General User Security Filter Chain
    @Bean
    @Order(2) // Process this chain second
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
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
                ).permitAll() // Public pages
                .requestMatchers(
                    "/cart/**", 
                    "/checkout/**", 
                    "/my_orders/**"
                ).authenticated() // User protected pages
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") // General user login page
                .loginProcessingUrl("/login") // Process general login POST here
                .failureUrl("/login?error") // Redirect to general login on failure
                .successHandler(customAuthenticationSuccessHandler) // Use custom handler
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // General logout URL
                .logoutSuccessUrl("/login?logout") // Redirect to general login after logout
                .permitAll()
            );
        return http.build();
    }
}