package app.cluttermap.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import app.cluttermap.exception.auth.InvalidAuthenticationException;
import app.cluttermap.model.User;
import app.cluttermap.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service("authService")
public class AuthService {
    /* ------------- Constants ------------- */
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // Factory for JSON processing, using Gson for serialization and deserialization
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    // HTTP transport layer, used to send HTTP requests over the network
    private final NetHttpTransport transport = new NetHttpTransport();

    private final long EXPIRATION_TIME = 86400000; // 1 day

    /* ------------- Injected Dependencies ------------- */
    public final SecurityService securityService;
    private UserRepository userRepository;

    /* ------------- Configuration Values ------------- */
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${security.jwt.secret-key}")
    private String JWT_SECRET;

    /* ------------- Constructor ------------- */
    public AuthService(SecurityService securityService, UserRepository userRepository) {
        this.securityService = securityService;
        this.userRepository = userRepository;
    }

    /* ------------- Token Verification ------------- */
    public GoogleIdToken verifyGoogleToken(String idTokenString) throws GeneralSecurityException, IOException {
        try {
            // Create the google id token verifier
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    // Set the audience to our client id
                    // this verifies that the token was issued for our app
                    .setAudience(Collections.singletonList(clientId))
                    .build();

            // Verify the token
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new InvalidAuthenticationException("Invalid ID token.");
            }
            return idToken;
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            throw new InvalidAuthenticationException("Invalid ID Token.");
        }
    }

    /* ------------- User Management ------------- */
    public User findOrCreateUserFromGoogleToken(GoogleIdToken idToken) {
        // Get user information from the payload
        GoogleIdToken.Payload payload = idToken.getPayload();
        String userGoogleId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");

        // OTHER INFORMATION WE COULD STORE IF WE WANT IN THE FUTURE
        // boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
        // String pictureUrl = (String) payload.get("picture");
        // String locale = (String) payload.get("locale");

        return userRepository.findByProviderId(userGoogleId).orElseGet(() -> {
            // If the user does not exist (first login), create a new user
            User newUser = new User(userGoogleId);
            newUser.setEmail(email);
            newUser.setProvider("google");
            newUser.setUsername(name);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);

            // Log the profile information
            logger.info("\nNewUser:\nGoogle ID: " + userGoogleId + "\nEmail: " + email + "\nName: " + name);
            // TODO email admin about new user

            return userRepository.save(newUser);
        });
    }

    /* ------------- Token Generation ------------- */
    public String generateJwtToken(User user) {
        // Securely convert the JWT_SECRET into a key used for signing the JWT using
        // HMAC SHA-256.
        // This helps ensure that the JWT is tamper-proof once signed.
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        // Create our own JWT
        // https://www.baeldung.com/java-json-web-tokens-jjwt
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
