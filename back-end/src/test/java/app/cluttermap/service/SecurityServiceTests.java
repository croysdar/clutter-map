package app.cluttermap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.TestDataFactory;
import app.cluttermap.exception.auth.InvalidAuthenticationException;
import app.cluttermap.exception.auth.UserNotFoundException;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.ProjectRepository;
import app.cluttermap.repository.RoomRepository;
import app.cluttermap.repository.UserRepository;
import app.cluttermap.util.ResourceType;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class SecurityServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private OrgUnitRepository orgUnitRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private EntityResolutionService entityResolutionService;

    @InjectMocks
    private SecurityService securityService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
        mockUser.setId(1L);
    }

    private void setUpJwtAuthentication(Long userId) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId.toString());
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    @Test
    void getCurrentUser_ShouldReturnCurrentUser_WhenAuthenticationIsValid() {
        // Arrange: Set up a valid JWT authentication token
        setUpJwtAuthentication(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Act: Call getCurrentUser
        User currentUser = securityService.getCurrentUser();

        // Assert: Verify that the correct user is returned
        assertNotNull(currentUser);
        assertEquals(mockUser.getId(), currentUser.getId());
    }

    @Test
    void getCurrentUser_ShouldThrowException_WhenAuthenticationIsInvalid() {
        // Arrange: Set an invalid authentication token in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(null);

        // Act & Assert: Verify that an exception is thrown
        assertThrows(InvalidAuthenticationException.class, () -> securityService.getCurrentUser());
    }

    @Test
    void getCurrentUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange: Set up a valid JWT authentication token
        setUpJwtAuthentication(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Verify that UserNotFoundException is thrown
        assertThrows(UserNotFoundException.class, () -> securityService.getCurrentUser());
    }

    @Test
    void isResourceOwner_ShouldReturnTrue_WhenUserOwnsEntity() {
        // Arrange
        setUpJwtAuthentication(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        Project project = new TestDataFactory.ProjectBuilder().user(mockUser).build();
        // project.setId(1L);

        when(entityResolutionService.resolveProject(ResourceType.PROJECT, 1L)).thenReturn(project);

        // Act
        boolean isOwner = securityService.isResourceOwner(1L, ResourceType.PROJECT);

        // Assert
        assertTrue(isOwner);
        verify(entityResolutionService, times(1)).resolveProject(ResourceType.PROJECT, 1L);
    }

    @Test
    void isResourceOwner_ShouldReturnFalse_WhenUserDoesNotOwnEntity() {
        // Arrange
        setUpJwtAuthentication(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        User anotherUser = new User("otherProviderId");
        anotherUser.setId(2L);

        Project project = new TestDataFactory.ProjectBuilder().user(anotherUser).build();
        when(entityResolutionService.resolveProject(ResourceType.PROJECT, 1L)).thenReturn(project);

        // Act
        boolean isOwner = securityService.isResourceOwner(1L, ResourceType.PROJECT);

        // Assert
        assertFalse(isOwner);
        verify(entityResolutionService, times(1)).resolveProject(ResourceType.PROJECT, 1L);
    }
}