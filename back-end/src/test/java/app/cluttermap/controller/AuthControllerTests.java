package app.cluttermap.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import app.cluttermap.TestContainerConfig;
import app.cluttermap.exception.auth.InvalidAuthenticationException;
import app.cluttermap.model.User;
import app.cluttermap.service.AuthService;
import app.cluttermap.service.SecurityService;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

@WebMvcTest(AuthController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityService securityService;

    @MockBean
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

        // Generate a secure JWT_SECRET and set it in authService
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        JWT_SECRET = Encoders.BASE64.encode(key.getEncoded());
        ReflectionTestUtils.setField(authService, "JWT_SECRET", JWT_SECRET);
    }

    @Test
    void verifyGoogleToken_ShouldReturnJwtToken_WhenTokenIsValid() throws Exception {
        // Arrange: Mock valid Google ID token and user creation
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
        when(authService.verifyGoogleToken(anyString())).thenReturn(mockIdToken);
        when(authService.findOrCreateUserFromGoogleToken(mockIdToken)).thenReturn(mockUser);
        when(authService.generateJwtToken(mockUser)).thenReturn("mockJwtToken");

        // Act & Assert: Perform POST request and verify response
        mockMvc.perform(post("/auth/verify-token/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("validGoogleTokenString"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockJwtToken"));
    }

    @Test
    void verifyGoogleToken_ShouldReturnBadRequest_WhenTokenIsInvalid() throws Exception {
        // Arrange: Simulate invalid token exception
        when(authService.verifyGoogleToken(anyString()))
                .thenThrow(new InvalidAuthenticationException("Invalid ID token."));

        // Act & Assert: Perform POST request and verify response
        mockMvc.perform(post("/auth/verify-token/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalidGoogleTokenString"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserInfo_ShouldReturnUserInfo_WhenUserIsAuthenticated() throws Exception {
        // Arrange: Set up SecurityService to return the authenticated user
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        // Act & Assert: Perform GET request and verify user info in response
        mockMvc.perform(get("/auth/user-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.userName").value("testUser"))
                .andExpect(jsonPath("$.userFirstName").value("Test"))
                .andExpect(jsonPath("$.userLastName").value("User"));
    }

    @Test
    void getUserInfo_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() throws Exception {
        // Arrange: Simulate authentication failure by throwing an exception
        when(securityService.getCurrentUser())
                .thenThrow(new InvalidAuthenticationException("User is not authenticated."));

        // Act & Assert: Perform GET request and verify response status
        mockMvc.perform(get("/auth/user-info"))
                .andExpect(status().isUnauthorized());
    }
}
