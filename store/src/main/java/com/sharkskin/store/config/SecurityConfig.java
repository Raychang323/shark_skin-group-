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
                    "/images/**",
                    "/admin/login",
                    "/admin/logout"
                ).permitAll()
                .requestMatchers(
                    "/cart/**", 
                    "/checkout/**", 
                    "/my_orders/**", 
                    "/admin/**",
                    "/portal/a9x3z7/dashboard"
                ).authenticated()
                .anyRequest().authenticated()
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
