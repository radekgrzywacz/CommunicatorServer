package com.communicator.config.auth;


import com.communicator.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Configuration class for security settings.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private CustomLogoutHandler customLogoutHandler;

    @Autowired
    private JWTAuthFilter jwtAuthFilter;

    /**
     * Configures the security filter chain, defining the authentication and authorization behavior
     * for HTTP requests. This method disables CSRF protection, defines URL access rules, sets
     * custom user details service, manages session behavior, and adds JWT-based authentication.
     *
     * <p>This security configuration ensures that all requests except those to "/auth/**" require
     * authentication, and it customizes how authentication and access denials are handled.</p>
     *
     * @param http the {@link HttpSecurity} object used to configure the security settings for the
                application.
     * @return a configured {@link SecurityFilterChain} instance.
     * @throws Exception if an error occurs during the configuration process.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .userDetailsService(userService)
                .exceptionHandling(e -> e.accessDeniedHandler(customAccessDeniedHandler).
                        authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(l -> l.logoutUrl("/logout").addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler((request, response, authentication) ->
                                SecurityContextHolder.clearContext()));

        return http.build();
    }



    /**
     * Creates a PasswordEncoder instance that uses BCrypt to encrypt user passwords.
     *
     * @return a new instance of {@link PasswordEncoder} that encrypts passwords using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures a custom authentication manager using the provided authentication configuration.
     *
     * @param authenticationConfiguration the configuration object for authentication
     * @return a custom {@link AuthenticationManager} instance
     * @throws Exception if there is a problem obtaining the authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
