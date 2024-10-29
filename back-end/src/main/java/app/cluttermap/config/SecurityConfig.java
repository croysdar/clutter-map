package app.cluttermap.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // Logger supports different levels (INFO, DEBUG, ERROR, WARN)
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    // Used to get the jwt secret token
    @Autowired
    private JwtConfig jwtConfig;

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Applying security configuration...");

        // Configure authorization rules
        http
                .authorizeHttpRequests(auth -> auth
                        // Allow requests to "/auth/verify-token" without authentication
                        .requestMatchers("/auth/verify-token/google", "/auth/user-info").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated())
                // Enable Cross-Origin Resource Sharing (CORS) with default configuration
                .cors(Customizer.withDefaults())

                .csrf(csrf -> csrf
                        // CSRF token in cookies
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/auth/verify-token/google"))

                // Configure the OAuth2 resource server to expect JWT tokens
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()));

        logger.info("Security configuration applied successfully.");

        // Build and return the SecurityFilterChain object
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Retrieve the JWT secret key from configuration
        String JWT_SECRET = jwtConfig.getSecretKey();

        // Create a SecretKey using the secret string with HmacSHA256 algorithm
        SecretKey key = new SecretKeySpec(JWT_SECRET.getBytes(), "HmacSHA256");

        // Create and return a JwtDecoder using the secret key
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    // Global CORS configuration
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                logger.info("CORS configuration being added...");

                // Allow cross-origin requests for all endpoints
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000", "https://clutter-map.app") 
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true); // Allow cookies and credentials
            }
        };
    }
}
