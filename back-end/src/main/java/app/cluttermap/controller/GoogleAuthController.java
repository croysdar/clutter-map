package app.cluttermap.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.common.base.Optional;

import app.cluttermap.model.User;
import app.cluttermap.repository.UsersRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Optionals;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

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

    @PostMapping("/verify-token")
    public ResponseEntity<String> verifyGoogleToken(@RequestBody String idTokenString) throws GeneralSecurityException, IOException {
        System.out.println("Verify Token Called");

        System.out.println(idTokenString);

        // Create the token verifier
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();

        // Verify the token
        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken != null) {
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

            // Log or process the profile information
            System.out.println("User ID: " + userId);
            System.out.println("Email: " + email);
            System.out.println("Name: " + name);


            // Look up the user by email
            // Optional<User> existingUser = usersRepository.findByEmail(email);

            // // If the user does not exist (first login), create a new user
            // if (!existingUser.isPresent()) {
            //     User user = new User();
            //     user.setEmail(userEmail);
            //     user.setProvider(provider);
            //     usersRepository.save(user);
            // }


            // Return the response to the frontend
            return ResponseEntity.ok("ID Token is valid for user: " + name);

        } else {
            // Invalid ID token
            return ResponseEntity.badRequest().body("Invalid ID token.");
        }
    }
}
