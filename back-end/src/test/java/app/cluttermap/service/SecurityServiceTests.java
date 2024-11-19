package app.cluttermap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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
import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.exception.auth.InvalidAuthenticationException;
import app.cluttermap.exception.auth.UserNotFoundException;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.ProjectRepository;
import app.cluttermap.repository.RoomRepository;
import app.cluttermap.repository.UserRepository;

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
    void isResourceOwner_ShouldReturnTrue_WhenUserOwnsProject() {
        // Arrange: Mock the current user and a project owned by that user
        setUpJwtAuthentication(1L);
        Project project = new Project("Test Project", mockUser);
        project.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act: Check if the user owns the project
        boolean isOwner = securityService.isResourceOwner(1L, "project");

        // Assert: Verify that the user is identified as the owner
        assertTrue(isOwner);
    }

    @Test
    void isResourceOwner_ShouldThrowException_WhenProjectNotFound() {
        // Arrange: Mock the current user
        setUpJwtAuthentication(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Verify that ProjectNotFoundException is thrown
        assertThrows(ResourceNotFoundException.class, () -> securityService.isResourceOwner(1L, "project"));
    }

    @Test
    void isResourceOwner_ShouldReturnTrue_WhenUserOwnsRoom() {
        // Arrange: Mock the current user, project, and room owned by that user
        setUpJwtAuthentication(1L);
        Project project = new Project("Test Project", mockUser);
        Room room = new Room("Test Room", "", project);
        room.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // Act: Check if the user owns the room
        boolean isOwner = securityService.isResourceOwner(1L, "room");

        // Assert: Verify that the user is identified as the owner
        assertTrue(isOwner);
    }

    @Test
    void isResourceOwner_ShouldThrowException_WhenRoomNotFound() {
        // Arrange: Mock the current user
        setUpJwtAuthentication(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Verify that RoomNotFoundException is thrown
        assertThrows(ResourceNotFoundException.class, () -> securityService.isResourceOwner(1L, "room"));
    }

    @Test
    void isResourceOwner_ShouldReturnTrue_WhenUserOwnsOrgUnit() {
        // Arrange: Mock the current user, project, room, and org unit
        setUpJwtAuthentication(1L);
        Project project = new Project("Test Project", mockUser);
        Room room = new Room("Test Room", "", project);
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().room(room).build();
        orgUnit.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(orgUnit));

        // Act: Check if the user owns the org unit
        boolean isOwner = securityService.isResourceOwner(1L, "org-unit");

        // Assert: Verify that the user is identified as the owner
        assertTrue(isOwner);
    }

    @Test
    void isResourceOwner_ShouldThrowException_WhenOrgUnitNotFound() {
        // Arrange: Mock the current user
        setUpJwtAuthentication(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Verify that OrgUnitNotFoundException is thrown
        assertThrows(ResourceNotFoundException.class, () -> securityService.isResourceOwner(1L, "org-unit"));
    }

    @Test
    void isResourceOwner_ShouldReturnTrue_WhenUserOwnsItem() {
        // Arrange: Mock the current user, project, room, org unit, and item
        setUpJwtAuthentication(1L);
        Project project = new Project("Test Project", mockUser);
        Room room = new Room("Test Room", "", project);
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().room(room).build();
        Item item = new TestDataFactory.ItemBuilder().orgUnit(orgUnit).build();

        item.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act: Check if the user owns the item
        boolean isOwner = securityService.isResourceOwner(1L, "item");

        // Assert: Verify that the user is identified as the owner
        assertTrue(isOwner);
    }

    @Test
    void isResourceOwner_ShouldThrowException_WhenItemNotFound() {
        // Arrange: Mock the current user
        setUpJwtAuthentication(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Verify that ResourceNotFoundException is thrown
        assertThrows(ResourceNotFoundException.class, () -> securityService.isResourceOwner(1L, "item"));
    }
}