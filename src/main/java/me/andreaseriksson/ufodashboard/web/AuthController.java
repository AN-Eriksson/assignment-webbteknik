package me.andreaseriksson.ufodashboard.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/")
    Map<String, String> home() {
        return Map.of(
                "message", "WT backend is running",
                "login", "/oauth2/authorization/github",
                "me", "/api/auth/me",
                "logout", "/logout"
        );
    }

    @GetMapping("/api/auth/me")
    @ResponseStatus(HttpStatus.OK)
    Map<String, Object> me(Authentication authentication) {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("authenticated", true);
        response.put("name", oauthUser.getAttribute("name"));
        response.put("login", oauthUser.getAttribute("login"));
        response.put("email", oauthUser.getAttribute("email"));
        response.put("id", oauthUser.getAttribute("id"));
        response.put("avatarUrl", oauthUser.getAttribute("avatar_url"));
        return response;
    }
}

