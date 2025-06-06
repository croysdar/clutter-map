package app.cluttermap.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import app.cluttermap.model.User;
import app.cluttermap.service.AuthService;
import app.cluttermap.service.SecurityService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    /* ------------- Injected Dependencies ------------- */
    public final SecurityService securityService;
    public final AuthService authService;

    /* ------------- Constructor ------------- */
    public AuthController(SecurityService securityService, AuthService authService) {
        this.securityService = securityService;
        this.authService = authService;
    }

    /* ------------- Configuration Values ------------- */
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${security.jwt.secret-key}")
    private String JWT_SECRET;

    /* ------------- Authentication Operations ------------- */
    // This is called after a user logs in with the "Sign in with Google" button
    // Google returns a token to the frontend and we now want to verify it
    @PostMapping("/verify-token/google")
    public ResponseEntity<?> verifyGoogleToken(@RequestBody String idTokenString)
            throws GeneralSecurityException, IOException {

        GoogleIdToken idToken = authService.verifyGoogleToken(idTokenString);

        User user = authService.findOrCreateUserFromGoogleToken(idToken);

        String jwtToken = authService.generateJwtToken(user);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);

        return ResponseEntity.ok(response);
    }

    /* ------------- User Information Retrieval ------------- */
    // Used to both validate a JWT token (issued from this backend)
    // as well as get simple user info to return to the frontend
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        User user = securityService.getCurrentUser();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userEmail", user.getEmail());
        userInfo.put("userName", user.getUsername());
        userInfo.put("userFirstName", user.getFirstName());
        userInfo.put("userLastName", user.getLastName());

        return ResponseEntity.ok(userInfo);
    }
}
