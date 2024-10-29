package app.cluttermap.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import app.cluttermap.model.User;
import app.cluttermap.repository.UsersRepository;
import app.cluttermap.service.SecurityService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@RestController
@RequestMapping("/auth")
public class AuthController {
    // Logger supports different levels (INFO, DEBUG, ERROR, WARN)
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // Factory for JSON processing, using Gson for serialization and deserialization
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    // HTTP transport layer, used to send HTTP requests over the network
    private final NetHttpTransport transport = new NetHttpTransport();

    public final SecurityService securityService;

    public AuthController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Autowired
    private UsersRepository usersRepository;

    @Value("${security.jwt.secret-key}")
    private String JWT_SECRET;

    private final long EXPIRATION_TIME = 86400000; // 1 day

    // This is called after a user logs in with the "Sign in with Google" button
    // Google returns a token to the frontend and we now want to verify it
    @PostMapping("/verify-token/google")
    public ResponseEntity<?> verifyGoogleToken(@RequestBody String idTokenString)
            throws GeneralSecurityException, IOException {
        logger.info("Verify Token Called");

        // Create the google id token verifier
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                // Set the audience to our client id
                // this verifies that the token was issued for our app
                .setAudience(Collections.singletonList(clientId))
                .build();

        // Verify the token
        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken != null) {

            // Get user information from the payload
            GoogleIdToken.Payload payload = idToken.getPayload();
            String userGoogleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // OTHER INFORMATION WE COULD STORE IF WE WANT IN THE FUTURE
            // boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            // String pictureUrl = (String) payload.get("picture");
            // String locale = (String) payload.get("locale");
            // String familyName = (String) payload.get("family_name");
            // String givenName = (String) payload.get("given_name");

            // Log the profile information
            logger.info("\nGoogle ID: " + userGoogleId + "\nEmail: " + email + "\nName: " + name);

            // Look up the user by google id
            Optional<User> existingUser = usersRepository.findByProviderId(userGoogleId);

            // If the user does not exist (first login), create a new user
            User user = existingUser.orElseGet(() -> {
                User newUser = new User(userGoogleId);
                newUser.setEmail(email);
                newUser.setProvider("google");
                newUser.setUsername(name);
                return usersRepository.save(newUser);
            });

            // Securely convert the JWT_SECRET into a key used for signing the JWT using
            // HMAC SHA-256.
            // This helps ensure that the JWT is tamper-proof once signed.
            SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

            // Create our own JWT
            // https://www.baeldung.com/java-json-web-tokens-jjwt
            String jwtToken = Jwts.builder()
                    .setSubject(user.getId().toString())
                    .claim("email", user.getEmail())
                    .claim("username", user.getUsername())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            // Building response
            Map<String, String> response = new HashMap<>();
            response.put("token", jwtToken);
            response.put("userEmail", email);
            response.put("userName", name);

            return ResponseEntity.ok(response);
        } else {
            // Invalid ID token
            return ResponseEntity.badRequest().body("Invalid ID token.");
        }
    }

    // Used to both validate a JWT token (typically issued from this backend)
    // as well as get simple user info to return to the frontend
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        try {
            User user = securityService.getCurrentUser();

            // Build the response object
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userEmail", user.getEmail());
            userInfo.put("userName", user.getUsername());

            return new ResponseEntity<>(userInfo, HttpStatus.OK);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());

            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}
