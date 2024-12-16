package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import app.cluttermap.model.Event;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewOrgUnitDTO;
import app.cluttermap.model.dto.UpdateOrgUnitDTO;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.RoomRepository;
import app.cluttermap.util.ResourceType;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableTestcontainers
public class OrgUnitServiceSecurityTests {
    @Autowired
    private OrgUnitService orgUnitService;

    @MockBean
    private RoomService roomService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private OrgUnitRepository orgUnitRepository;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private EventService eventService;

    @MockBean
    private SecurityService securityService;

    private Project mockProject;
    private OrgUnit mockOrgUnit;

    @BeforeEach
    void setUp() {
        mockProject = createMockProject();
        mockOrgUnit = createMockOrgUnit(mockProject);

        when(securityService.isResourceOwner(anyLong(), any(ResourceType.class))).thenReturn(true);
        when(projectService.getProjectById(mockProject.getId())).thenReturn(mockProject);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Org Unit should be retrieved successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void getOrgUnitById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.ORGANIZATIONAL_UNIT;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Act: Call the method under test
            OrgUnit orgUnit = orgUnitService.getOrgUnitById(resourceId);
            // Assert: OrgUnit should be retrieved successfully
            assertNotNull(orgUnit, description);
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.getOrgUnitById(resourceId),
                    description);
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Unassigned org units should be retrieved when user has ownership of the project",
            "false, AccessDeniedException should be thrown when user lacks ownership of the project"
    })
    @WithMockUser(username = "testUser")
    void getUnassignedOrgUnitsByProjectId_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.PROJECT;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock repository behavior for authorized access
            when(orgUnitRepository.findUnassignedOrgUnitsByProjectId(mockProject.getId()))
                    .thenReturn(List.of(mockOrgUnit));

            // Act: Call the method under test
            List<OrgUnit> orgUnits = orgUnitService.getUnassignedOrgUnitsByProjectId(mockProject.getId());

            // Assert: Validate retrieved orgUnits
            assertAll(
                    () -> assertNotNull(orgUnits, description),
                    () -> assertEquals(1, orgUnits.size(), "OrgUnits list should contain exactly 1 org unit."));
            verify(orgUnitRepository).findUnassignedOrgUnitsByProjectId(resourceId);

        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.getUnassignedOrgUnitsByProjectId(mockProject.getId()),
                    description);
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, PROJECT, Item should be created successfully when user has ownership of the project",
            "false, PROJECT, AccessDeniedException should be thrown when user lacks ownership of the project",
            "true, ROOM, Org Unit should be created successfully when user has ownership of the room",
            "false, ROOM, AccessDeniedException should be thrown when user lacks ownership of the org unit",
    })
    @WithMockUser(username = "testUser")
    void createOrgUnit_ShouldRespectOwnership(boolean isOwner, ResourceType resourceType, String description) {
        // Arrange: Prepare mock data based on the resource type
        Long resourceId;
        NewOrgUnitDTO orgUnitDTO;

        if (resourceType.equals(ResourceType.PROJECT)) {
            resourceId = mockProject.getId();
            orgUnitDTO = new TestDataFactory.NewOrgUnitDTOBuilder()
                    .projectId(mockProject.getId())
                    .roomId(null)
                    .build();
        } else {
            Room mockRoom = createMockRoom(mockProject);
            resourceId = mockRoom.getId();
            orgUnitDTO = new TestDataFactory.NewOrgUnitDTOBuilder()
                    .projectId(null)
                    .roomId(mockRoom.getId())
                    .build();
        }

        // Arrange: Configure security service
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock repository behavior for authorized access
            when(orgUnitRepository.save(any(OrgUnit.class))).thenReturn(mockOrgUnit);

            // Arrange: Mock event logging
            mockLogCreateEvent();

            // Act: Call the method under test
            OrgUnit orgUnit = orgUnitService.createOrgUnit(orgUnitDTO);

            // Assert: Validate orgUnit creation
            assertNotNull(orgUnit, description);
            verify(orgUnitRepository).save(any(OrgUnit.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.createOrgUnit(orgUnitDTO),
                    description);
            // Verify: Ensure orgUnit repository save is never invoked
            verify(orgUnitRepository, never()).save(any(OrgUnit.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Org Unit should be updated successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void updateOrgUnit_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.ORGANIZATIONAL_UNIT;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        UpdateOrgUnitDTO orgUnitDTO = new TestDataFactory.UpdateOrgUnitDTOBuilder().build();

        if (isOwner) {
            // Mock repository behavior for authorized access
            when(orgUnitRepository.save(any(OrgUnit.class))).thenReturn(mockOrgUnit);

            // Arrange: Mock event logging
            mockLogUpdateEvent();

            // Act: Call the method under test
            OrgUnit orgUnit = orgUnitService.updateOrgUnit(resourceId, orgUnitDTO);

            // Assert: Validate successful update
            assertNotNull(orgUnit, description);
            verify(orgUnitRepository).save(any(OrgUnit.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.updateOrgUnit(resourceId, orgUnitDTO),
                    description);
            // Verify: Ensure orgUnit repository save is never invoked
            verify(orgUnitRepository, never()).save(any(OrgUnit.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Org Units should be assigned to room successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void assignOrgUnitsToRoom_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.ORGANIZATIONAL_UNIT;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        Room mockRoom = createMockRoom(mockProject);

        if (isOwner) {
            // Act: Call the method under test
            Iterable<OrgUnit> orgUnits = orgUnitService.assignOrgUnitsToRoom(List.of(resourceId), mockOrgUnit.getId());

            // Assert: Validate successful assignment
            assertAll(
                    () -> assertNotNull(orgUnits, "OrgUnits list should not be null when the user has ownership."),
                    () -> assertEquals(mockRoom, mockOrgUnit.getRoom(),
                            description));

            // Verify: Ensure room retrieval occurred
            verify(roomService).getRoomById(mockRoom.getId());
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.assignOrgUnitsToRoom(List.of(resourceId), mockOrgUnit.getId()),
                    description);

            // Verify: Ensure orgUnit repository save is never invoked
            verify(orgUnitRepository, never()).save(any(OrgUnit.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Org Units should be unassigned successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void unassignOrgUnits_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.ORGANIZATIONAL_UNIT;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock interaction with orgUnit repository for authorized access
            // Ensure orgUnit is set up properly for unassignment
            mockOrgUnitWithRoom();

            // Act: Call the method under test
            Iterable<OrgUnit> orgUnits = orgUnitService.unassignOrgUnits(List.of(1L));

            // Assert: Validate successful unassignment
            assertNotNull(orgUnits, description);
            verify(roomRepository).save(any(Room.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.unassignOrgUnits(List.of(resourceId)),
                    description);

            // Verify: Ensure orgUnit repository save is never invoked
            verify(orgUnitRepository, never()).save(any(OrgUnit.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Org Unit should be deleted successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void deleteById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.ORGANIZATIONAL_UNIT;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock repository behavior for authorized access
            doNothing().when(orgUnitRepository).deleteById(1L);

            // Arrange: Mock event logging
            mockLogDeleteEvent();

            // Act: Call the method under test
            orgUnitService.deleteOrgUnitById(1L);

            // Assert: Validate successful deletion
            assertThatCode(() -> verify(orgUnitRepository).delete(any(OrgUnit.class)))
                    .as(description)
                    .doesNotThrowAnyException();
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.deleteOrgUnitById(resourceId),
                    description);
            // Verify: Ensure orgUnit repository save is never invoked
            verify(orgUnitRepository, never()).deleteById(1L);
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    private Project createMockProject() {
        User user = new User("mockProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(user).build();

        return project;
    }

    private Room createMockRoom(Project project) {
        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        when(roomService.getRoomById(room.getId())).thenReturn(room);

        return room;
    }

    private OrgUnit createMockOrgUnit(Project project) {
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().project(project).build();
        when(orgUnitRepository.findById(orgUnit.getId())).thenReturn(Optional.of(orgUnit));

        return orgUnit;
    }

    private void mockOrgUnitWithRoom() {
        Room mockRoom = createMockRoom(mockProject);
        mockOrgUnit.setRoom(mockRoom);
        when(orgUnitRepository.findById(mockOrgUnit.getId())).thenReturn(Optional.of(mockOrgUnit));
    }

    private void mockLogCreateEvent() {
        when(eventService.logCreateEvent(any(), anyLong(), any())).thenReturn(new Event());
    }

    private void mockLogUpdateEvent() {
        when(eventService.logUpdateEvent(any(), anyLong(), any())).thenReturn(new Event());
    }

    private void mockLogDeleteEvent() {
        when(eventService.logDeleteEvent(any(), anyLong())).thenReturn(new Event());
    }
}
