package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.EnableTestcontainers;
import app.cluttermap.TestDataFactory;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;
import app.cluttermap.repository.RoomRepository;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableTestcontainers
public class RoomServiceSecurityTests {
    @Autowired
    private RoomService roomService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private SecurityService securityService;

    private Project mockProject;
    private Room mockRoom;

    @BeforeEach
    void setUp() {
        mockProject = createMockProject();
        mockRoom = createMockRoom(mockProject);

        when(securityService.isResourceOwner(anyLong(), anyString())).thenReturn(true);
        when(projectService.getProjectById(mockProject.getId())).thenReturn(mockProject);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Room should be retrieved successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void getRoomById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "room";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Act: Call the method under test
            Room room = roomService.getRoomById(resourceId);
            // Assert: Room should be retrieved successfully
            assertNotNull(room, description);
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> roomService.getRoomById(resourceId),
                    description);
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Room should be created successfully when user has ownership of the project",
            "false,AccessDeniedException should be thrown when user lacks ownership of the project",
    })
    @WithMockUser(username = "testUser")
    void createRoom_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data based on the resource type
        Long resourceId = mockProject.getId();

        String resourceType = "project";
        NewRoomDTO roomDTO;

        roomDTO = new TestDataFactory.NewRoomDTOBuilder()
                .projectId(mockProject.getId())
                .build();

        // Arrange: Configure security service
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock repository behavior for authorized access
            when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);

            // Act: Call the method under test
            Room room = roomService.createRoom(roomDTO);

            // Assert: Validate room creation
            assertNotNull(room, description);
            verify(roomRepository).save(any(Room.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> roomService.createRoom(roomDTO),
                    description);
            // Verify: Ensure room repository save is never invoked
            verify(roomRepository, never()).save(any(Room.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Room should be updated successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void updateRoom_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "room";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        UpdateRoomDTO roomDTO = new TestDataFactory.UpdateRoomDTOBuilder().build();

        if (isOwner) {
            // Mock repository behavior for authorized access
            when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);

            // Act: Call the method under test
            Room room = roomService.updateRoom(resourceId, roomDTO);

            // Assert: Validate successful update
            assertNotNull(room, description);
            verify(roomRepository).save(any(Room.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> roomService.updateRoom(resourceId, roomDTO),
                    description);
            // Verify: Ensure room repository save is never invoked
            verify(roomRepository, never()).save(any(Room.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Room should be deleted successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void deleteById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "room";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock repository behavior for authorized access
            doNothing().when(roomRepository).deleteById(1L);

            // Act: Call the method under test
            roomService.deleteRoomById(resourceId);

            // Assert: Validate successful deletion
            assertThatCode(() -> verify(roomRepository).delete(any(Room.class)))
                    .as(description)
                    .doesNotThrowAnyException();

        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> roomService.deleteRoomById(resourceId),
                    description);
            // Verify: Ensure room repository save is never invoked
            verify(roomRepository, never()).deleteById(1L);
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    private Project createMockProject() {
        User user = new User("mockProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(user).build();
        project.setId(1L);

        return project;
    }

    private Room createMockRoom(Project project) {
        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        room.setId(1L);
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        return room;
    }

}
