package me.andreaseriksson.ufodashboard.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 *
 * Provides endpoints for the frontend to check authentication status, retrieve authenticated user
 * information, and perform logout. These endpoints are part of the OAuth2 flow:
 *
 * 1. After OAuth2 login (handled by SecurityConfig), frontend calls /api/auth/check
 * 2. Frontend receives authenticated user data and stores it locally
 * 3. Authenticated user can call /api/auth/me to fetch their profile
 * 4. User can call /api/auth/logout to clear session and redirect home
 *
 * All methods return user data as JSON. The check() and me() methods return false/throw
 * if the user is not authenticated (handled by Spring Security).
 */
@RestController
public class AuthController {

    /**
     * Checks if the current user is authenticated and returns their profile if so.
     *
     * This endpoint is used by the frontend after page load to determine if a user is logged in.
     * It either returns the authenticated user's profile or an authenticated:false response.
     * No authentication is required for this endpoint - the frontend needs to check auth status.
     *
     * Response (authenticated user):
     *   {
     *     "authenticated": true,
     *     "name": "John Doe",
     *     "login": "johndoe",
     *     "email": "john@example.com",
     *     "id": 12345,
     *     "avatarUrl": "https://avatars.githubusercontent.com/u/12345"
     *   }
     *
     * Response (no authentication):
     *   {
     *     "authenticated": false
     *   }
     *
     * @param authentication the Spring Security Authentication object (may be null/unauthenticated)
     * @return a Map with authentication status and user details
     */
    @GetMapping("/api/auth/check")
    @ResponseStatus(HttpStatus.OK)
    Map<String, Object> check(Authentication authentication) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof OAuth2User oauthUser) {
            response.put("authenticated", true);
            response.put("name", attributeAsString(oauthUser, "name"));
            response.put("login", attributeAsString(oauthUser, "login"));
            response.put("email", attributeAsString(oauthUser, "email"));
            response.put("id", oauthUser.getAttribute("id"));
            response.put("avatarUrl", attributeAsString(oauthUser, "avatar_url"));
        } else {
            response.put("authenticated", false);
        }

        return response;
    }

    /**
     * Returns the current authenticated user's profile information.
     *
     * This endpoint requires authentication. The frontend can call this after confirming
     * the user is logged in (via check() endpoint) to fetch the full user profile.
     *
     * Returns the authenticated user's OAuth2 profile including name, email, GitHub login,
     * user ID, and avatar URL.
     *
     * Response:
     *   {
     *     "authenticated": true,
     *     "name": "John Doe",
     *     "login": "johndoe",
     *     "email": "john@example.com",
     *     "id": 12345,
     *     "avatarUrl": "https://avatars.githubusercontent.com/u/12345"
     *   }
     *
     * @param authentication the Spring Security Authentication object (must be authenticated)
     * @return a Map with the current user's profile
     */
    @GetMapping("/api/auth/me")
    @ResponseStatus(HttpStatus.OK)
    Map<String, Object> me(Authentication authentication) {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("authenticated", true);
        response.put("name", attributeAsString(oauthUser, "name"));
        response.put("login", attributeAsString(oauthUser, "login"));
        response.put("email", attributeAsString(oauthUser, "email"));
        response.put("id", oauthUser.getAttribute("id"));
        response.put("avatarUrl", attributeAsString(oauthUser, "avatar_url"));
        return response;
    }

    /**
     * Extracts an attribute from an OAuth2User and converts it to a String.
     *
     * Helper method to safely retrieve OAuth2 user attributes. Returns null if the attribute
     * does not exist or is null, otherwise converts the attribute value to a String.
     *
     * @param user the OAuth2User from GitHub
     * @param attributeName the name of the attribute to retrieve (e.g. "name", "email", "login")
     * @return the attribute value as a String, or null if not found/null
     */
    private String attributeAsString(OAuth2User user, String attributeName) {
        Object value = user.getAttribute(attributeName);
        return value != null ? value.toString() : null;
    }

    /**
     * Logs out the current user and clears their session.
     *
     * Performs the following cleanup on the server side:
     *   1. Invalidates the HTTP session
     *   2. Clears the JSESSIONID cookie by setting max-age to 0
     *   3. Redirects the user to the home page
     *
     * The frontend should not call API endpoints after logout. Spring Security will
     * reject unauthenticated requests to protected endpoints.
     *
     * @param request the HTTP request object
     * @param response the HTTP response object
     * @throws IOException if a redirect fails
     */
    @GetMapping("/api/auth/logout")
    void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        response.sendRedirect("/");
    }
}

