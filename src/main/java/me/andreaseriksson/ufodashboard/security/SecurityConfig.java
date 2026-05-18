package me.andreaseriksson.ufodashboard.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import java.util.Arrays;

/**
 * Security configuration for the UFO Dashboard application.
 *
 * Configures the OAuth2 authentication flow and HTTP security. The authentication flow works as follows:
 *   1. User clicks "Log in with GitHub" button on the frontend
 *   2. The user is redirected to GitHub's OAuth authorization endpoint
 *   3. User approves the application's access request on GitHub
 *   4. GitHub redirects back to the dashboard with an authorization code
 *   5. Spring Security (on the backend) exchanges the code for user details via GitHub API
 *   6. User is authenticated and receives a Spring session
 *   7. Frontend calls `/api/auth/check` to verify authentication and load user info
 *   8. All subsequent API requests require authentication
 *
 * Public Endpoints (no authentication required):
 *   - Static files: `/`, `/index.html`, `/assets/**`
 *   - OAuth endpoints: `/oauth2/**`, `/login/**`
 *   - Auth check: `/api/auth/check`, `/api/auth/logout`
 *
 * Protected Endpoints (authentication required):
 *   - User info: `/api/auth/me`
 *   - Data endpoints: `/api/sightings/**`, `/api/shapes/**`, `/api/locations/**`
 *
 * CORS Configuration:
 * Cross-Origin Resource Sharing is enabled to allow the frontend (development and production)
 * to make requests to the backend API. Origins are configurable via the `app.cors.allowed-origins` property.
 *
 * @see org.springframework.security.oauth2.client.OAuth2AuthorizedClient
 */
@Configuration
public class SecurityConfig {

    /**
     * Comma-separated list of allowed CORS origins.
     * Defaults to localhost development servers if not configured.
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8080}")
    private String allowedOrigins;

    /**
     * Configures the HTTP security filter chain for the application.
     *
     * Configuration includes:
     *   - CORS: Allows cross-origin requests from configured origins
     *   - CSRF: Disables CSRF protection for `/logout` endpoint
     *   - Authorization: Permits public endpoints, requires authentication for all others
     *   - OAuth2 Login: Enables GitHub OAuth2 authentication with redirect to home on success
     *   - Logout: Clears session, cookies, and authentication state
     *   - Disabled: Form login and HTTP Basic authentication
     *
     * @param http the HttpSecurity to configure
     * @return a configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/logout")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/assets/**", "/error", "/favicon.ico", "/oauth2/**", "/login/**", "/api/auth/check", "/api/auth/logout").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutRequestMatcher(request -> "/logout".equals(request.getRequestURI())
                                && ("GET".equals(request.getMethod()) || "POST".equals(request.getMethod())))
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .oauth2Client(Customizer.withDefaults());

        return http.build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings for the application.
     *
     * Enables the frontend (running on a different origin during development) to make requests
     * to the backend API. The configuration allows:
     *   - Configured origins to make requests (set via `app.cors.allowed-origins`)
     *   - HTTP methods: GET, POST, PUT, DELETE, OPTIONS
     *   - Any HTTP headers
     *   - Credentials (cookies) to be sent with cross-origin requests
     *
     * Security Note: Credentials are allowed because the frontend needs to send the session
     * cookie with authenticated API requests.
     *
     * @return a configured CorsConfigurationSource
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

