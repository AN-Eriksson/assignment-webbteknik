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

@RestController
public class AuthController {


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

    private String attributeAsString(OAuth2User user, String attributeName) {
        Object value = user.getAttribute(attributeName);
        return value != null ? value.toString() : null;
    }

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

