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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import app.cluttermap.model.User;
import app.cluttermap.repository.UsersRepository;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class GoogleAuthController {

    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private final NetHttpTransport transport = new NetHttpTransport();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Autowired
    private UsersRepository usersRepository;

    @Value("${security.jwt.secret-key}")
    private String JWT_SECRET;

    private final long EXPIRATION_TIME = 86400000; // 1 day

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyGoogleToken(@RequestBody String idTokenString)
            throws GeneralSecurityException, IOException {
        System.out.println("Verify Token Called");

        // Create the token verifier
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();

        // Verify the token
        GoogleIdToken idToken = verifier.verify(idTokenString);


        if (idToken != null) {
            System.out.println("Token Verified");

            GoogleIdToken.Payload payload = idToken.getPayload();

            // Get user information from the payload
            String userId = payload.getSubject();
            String email = payload.getEmail();
            // boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            // String pictureUrl = (String) payload.get("picture");
            // String locale = (String) payload.get("locale");
            // String familyName = (String) payload.get("family_name");
            // String givenName = (String) payload.get("given_name");

            // Log the profile information
            System.out.println("User ID: " + userId);
            System.out.println("Email: " + email);
            System.out.println("Name: " + name);

            // Look up the user by email
            Optional<User> existingUser = usersRepository.findByGoogleId(userId);

            // If the user does not exist (first login), create a new user
            User user = existingUser.orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setProvider("google");
                newUser.setUsername(name);
                return usersRepository.save(newUser);
            });

            SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

            // Create our own JWT
            // https://www.baeldung.com/java-json-web-tokens-jjwt
            String jwtToken = Jwts.builder()
                    .setSubject(user.getId().toString()) // Set the subject as the user's unique ID
                    .claim("email", user.getEmail()) // Add claims (optional)
                    .claim("username", user.getUsername())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(key, SignatureAlgorithm.HS256) // Use your secret key
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
}
