package app.cluttermap.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewOrgUnitDTO;
import app.cluttermap.model.dto.UpdateOrgUnitDTO;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.RoomRepository;

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
    private SecurityService securityService;

    private Project mockProject;
    private OrgUnit mockOrgUnit;

    @BeforeEach
    void setUp() {
        mockProject = createMockProject();
        mockOrgUnit = createMockOrgUnit(mockProject);

        when(securityService.isResourceOwner(anyLong(), anyString())).thenReturn(true);
        when(projectService.getProjectById(mockProject.getId())).thenReturn(mockProject);
    }

    @ParameterizedTest
    @CsvSource({
            "true, getOrgUnitById_UserHasOwnership",
            "false, getOrgUnitById_UserLacksOwnership"
    })
    @WithMockUser(username = "testUser")
    void getOrgUnitById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "org-unit";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Act: Call the method under test
            OrgUnit orgUnit = orgUnitService.getOrgUnitById(resourceId);
            // Assert: OrgUnit should be retrieved successfully
            assertNotNull(orgUnit, "OrgUnit should not be null when the user has ownership.");
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.getOrgUnitById(resourceId),
                    "AccessDeniedException should be thrown when the user lacks ownership.");
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, getUnassignedOrgUnitsByProjectId_UserHasOwnership",
            "false, getUnassignedOrgUnitsByProjectId_UserLacksOwnership"
    })
    @WithMockUser(username = "testUser")
    void getUnassignedOrgUnitsByProjectId_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "project";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock repository behavior for authorized access
            when(orgUnitRepository.findUnassignedOrgUnitsByProjectId(mockProject.getId()))
                    .thenReturn(List.of(mockOrgUnit));

            // Act: Call the method under test
            List<OrgUnit> orgUnits = orgUnitService.getUnassignedOrgUnitsByProjectId(mockProject.getId());

            // Assert: Validate retrieved orgUnits
            assertAll(
                    () -> assertNotNull(orgUnits, "OrgUnits list should not be null when the user has ownership."),
                    () -> assertEquals(1, orgUnits.size(), "OrgUnits list should contain exactly 1 org unit."));
            verify(orgUnitRepository).findUnassignedOrgUnitsByProjectId(resourceId);

        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.getUnassignedOrgUnitsByProjectId(mockProject.getId()),
                    "AccessDeniedException should be thrown when the user lacks ownership.");
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, project, createOrgUnit_UserHasOwnership",
            "false, project, createOrgUnit_UserLacksOwnership",
            "true, room, createOrgUnit_UserHasOwnership",
            "false, room, createOrgUnit_UserLacksOwnership",
    })
    @WithMockUser(username = "testUser")
    void createOrgUnit_ShouldRespectOwnership(boolean isOwner, String resourceType, String description) {
        // Arrange: Prepare mock data based on the resource type
        Long resourceId;
        NewOrgUnitDTO orgUnitDTO;

        if (resourceType.equals("project")) {
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

            // Act: Call the method under test
            OrgUnit orgUnit = orgUnitService.createOrgUnit(orgUnitDTO);

            // Assert: Validate orgUnit creation
            assertNotNull(orgUnit, "OrgUnit should not be null when the user has ownership.");
            verify(orgUnitRepository).save(any(OrgUnit.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.createOrgUnit(orgUnitDTO),
                    "AccessDeniedException should be thrown when the user lacks ownership.");
            // Verify: Ensure orgUnit repository save is never invoked
            verify(orgUnitRepository, never()).save(any(OrgUnit.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, updateOrgUnit_UserHasOwnership",
            "false, updateOrgUnit_UserLacksOwnership"
    })
    @WithMockUser(username = "testUser")
    void updateOrgUnit_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "org-unit";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        UpdateOrgUnitDTO orgUnitDTO = new TestDataFactory.UpdateOrgUnitDTOBuilder().build();

        if (isOwner) {
            // Mock repository behavior for authorized access
            when(orgUnitRepository.save(any(OrgUnit.class))).thenReturn(mockOrgUnit);

            // Act: Call the method under test
            OrgUnit orgUnit = orgUnitService.updateOrgUnit(resourceId, orgUnitDTO);

            // Assert: Validate successful update
            assertNotNull(orgUnit, "OrgUnit should not be null when the user has ownership.");
            verify(orgUnitRepository).save(any(OrgUnit.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.updateOrgUnit(resourceId, orgUnitDTO),
                    "AccessDeniedException should be thrown when the user lacks ownership.");
            // Verify: Ensure orgUnit repository save is never invoked
            verify(orgUnitRepository, never()).save(any(OrgUnit.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, assignOrgUnitsToRoom_UserHasOwnership",
            "false, assignOrgUnitsToRoom_UserLacksOwnership"
    })
    @WithMockUser(username = "testUser")
    void assignOrgUnitsToRoom_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "org-unit";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        Room mockRoom = createMockRoom(mockProject);

        if (isOwner) {
            // Act: Call the method under test
            Iterable<OrgUnit> orgUnits = orgUnitService.assignOrgUnitsToRoom(List.of(resourceId), mockOrgUnit.getId());

            // Assert: Validate successful assignment
            assertAll(
                    () -> assertNotNull(orgUnits, "OrgUnits list should not be null when the user has ownership."),
                    () -> assertEquals(mockRoom, mockOrgUnit.getRoom(),
                            "OrgUnit's room should be updated to the target room."));

            // Verify: Ensure room retrieval occurred
            verify(roomService).getRoomById(mockRoom.getId());
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.assignOrgUnitsToRoom(List.of(resourceId), mockOrgUnit.getId()),
                    "AccessDeniedException should be thrown when the user lacks ownership.");

            // Verify: Ensure orgUnit repository save is never invoked
            verify(orgUnitRepository, never()).save(any(OrgUnit.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, unassignOrgUnits_UserHasOwnership",
            "false, unassignOrgUnits_UserLacksOwnership"
    })
    @WithMockUser(username = "testUser")
    void unassignOrgUnits_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "org-unit";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock interaction with orgUnit repository for authorized access
            // Ensure orgUnit is set up properly for unassignment
            mockOrgUnitWithRoom();

            // Act: Call the method under test
            Iterable<OrgUnit> orgUnits = orgUnitService.unassignOrgUnits(List.of(1L));

            // Assert: Validate successful unassignment
            assertNotNull(orgUnits, "OrgUnits list should not be null when the user has ownership.");
            verify(roomRepository).save(any(Room.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.unassignOrgUnits(List.of(resourceId)),
                    "AccessDeniedException should be thrown when the user lacks ownership.");

            // Verify: Ensure orgUnit repository save is never invoked
            verify(orgUnitRepository, never()).save(any(OrgUnit.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, deleteById_UserHasOwnership",
            "false, deleteById_UserLacksOwnership"
    })
    @WithMockUser(username = "testUser")
    void deleteById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "org-unit";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock repository behavior for authorized access
            doNothing().when(orgUnitRepository).deleteById(1L);

            // Act: Call the method under test
            orgUnitService.deleteOrgUnitById(1L);

            // Assert: Validate successful deletion
            verify(orgUnitRepository).delete(any(OrgUnit.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> orgUnitService.deleteOrgUnitById(resourceId),
                    "AccessDeniedException should be thrown when the user lacks ownership.");
            // Verify: Ensure orgUnit repository save is never invoked
            verify(orgUnitRepository, never()).deleteById(1L);
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
        when(roomService.getRoomById(room.getId())).thenReturn(room);

        return room;
    }

    private OrgUnit createMockOrgUnit(Project project) {
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().project(project).build();
        orgUnit.setId(1L);
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(orgUnit));

        return orgUnit;
    }

    private void mockOrgUnitWithRoom() {
        Room mockRoom = createMockRoom(mockProject);
        mockOrgUnit.setRoom(mockRoom);
        when(orgUnitRepository.findById(mockOrgUnit.getId())).thenReturn(Optional.of(mockOrgUnit));
    }
}
