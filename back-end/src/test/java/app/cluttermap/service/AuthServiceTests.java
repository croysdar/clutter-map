package app.cluttermap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import app.cluttermap.exception.auth.InvalidAuthenticationException;
import app.cluttermap.model.User;
import app.cluttermap.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private String JWT_SECRET;

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("testUser");
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");

        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        JWT_SECRET = Encoders.BASE64.encode(key.getEncoded());

        ReflectionTestUtils.setField(authService, "JWT_SECRET", JWT_SECRET);
    }

    @Test
    void verifyGoogleToken_ShouldThrowException_WhenTokenIsInvalid() {
        // Arrange: Mock invalid token
        String invalidToken = "invalidTokenString";

        // Act & Assert: Verify that an InvalidAuthenticationException is thrown
        assertThrows(InvalidAuthenticationException.class, () -> authService.verifyGoogleToken(invalidToken));
    }

    @Test
    void findOrCreateUserFromGoogleToken_ShouldCreateNewUser_WhenUserDoesNotExist() {
        // Arrange: Mock the GoogleIdToken and Payload
        GoogleIdToken mockToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = mock(GoogleIdToken.Payload.class);
        when(mockToken.getPayload()).thenReturn(mockPayload);

        when(mockPayload.getSubject()).thenReturn(mockUser.getProviderId());
        when(mockPayload.getEmail()).thenReturn(mockUser.getEmail());
        when(mockPayload.get("name")).thenReturn(mockUser.getUsername());
        when(mockPayload.get("given_name")).thenReturn(mockUser.getFirstName());
        when(mockPayload.get("family_name")).thenReturn(mockUser.getLastName());

        when(userRepository.findByProviderId(mockUser.getProviderId())).thenReturn(Optional.empty());
        // Mock the save method to return the same User object passed to it,
        // simulating repository behavior without generating new values like IDs.
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Find or create the user
        User user = authService.findOrCreateUserFromGoogleToken(mockToken);

        // Assert: Verify user fields are set as expected
        assertEquals(mockUser.getProviderId(), user.getProviderId());
        assertEquals("google", user.getProvider());
        assertEquals(mockUser.getEmail(), user.getEmail());
        assertEquals(mockUser.getUsername(), user.getUsername());
        assertEquals(mockUser.getFirstName(), user.getFirstName());
        assertEquals(mockUser.getLastName(), user.getLastName());
    }

    @Test
    void findOrCreateUserFromGoogleToken_ShouldReturnExistingUser_WhenUserExists() {
        // Arrange: Mock the GoogleIdToken and Payload
        GoogleIdToken mockToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = mock(GoogleIdToken.Payload.class);
        when(mockToken.getPayload()).thenReturn(mockPayload);

        when(mockPayload.getSubject()).thenReturn(mockUser.getProviderId());

        when(userRepository.findByProviderId(mockUser.getProviderId())).thenReturn(Optional.of(mockUser));

        // Act: Find or create the user
        User user = authService.findOrCreateUserFromGoogleToken(mockToken);

        // Assert: Verify that the existing user is returned
        assertEquals(mockUser, user);
    }

    @Test
    void generateJwtToken_ShouldGenerateValidToken_ForExistingUser() {
        // Act: Generate JWT token
        String token = authService.generateJwtToken(mockUser);

        // Assert: Verify token is generated and not null
        assertNotNull(token);

        // Decode the token to verify the claims
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

        assertEquals(mockUser.getId().toString(), claims.getBody().getSubject());
        assertEquals(mockUser.getEmail(), claims.getBody().get("email"));
        assertEquals(mockUser.getUsername(), claims.getBody().get("username"));
    }
}
