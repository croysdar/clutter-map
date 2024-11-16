package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.exception.org_unit.OrgUnitLimitReachedException;
import app.cluttermap.exception.org_unit.OrgUnitNotFoundException;
import app.cluttermap.exception.room.RoomNotFoundException;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewOrgUnitDTO;
import app.cluttermap.model.dto.UpdateOrgUnitDTO;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrgUnitServiceTests {
    @Mock
    private OrgUnitRepository orgUnitRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private ProjectService projectService;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private OrgUnitService orgUnitService;

    private User mockUser;
    private Project mockProject;
    private Room mockRoom;

    private static int ORG_UNIT_LIMIT = 100;

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
        mockProject = new Project("Mock Project", mockUser);
        mockRoom = new Room("Mock Room", "", mockProject);
        mockRoom.setId(1L);

    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUserDoesNotOwnOrgUnit() {
        // Overwrite the default stub for `isResourceOwner` to deny access to the org
        // unit
        List<Long> orgUnitIds = List.of(1L, 2L, 3L);
        when(securityService.isResourceOwner(anyLong(), eq("org-unit"))).thenReturn(false);

        // Act & Assert:
        assertThrows(AccessDeniedException.class, () -> {
            orgUnitService.checkOwnershipForOrgUnits(orgUnitIds);
        });
    }

    @Test
    void getOrgUnitId_ShouldReturnOrgUnit_WhenOrgUnitExists() {
        // Arrange: Set up a sample org unit and stub the repository to return it by ID
        OrgUnit orgUnit = new OrgUnit("Sample Org Unit", "Org Unit description", mockRoom);
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(orgUnit));

        // Act: Retrieve the org unit using the service method
        OrgUnit foundOrgUnit = orgUnitService.getOrgUnitById(1L);

        // Assert: Verify that the orgUnit retrieved matches the expected orgUnit
        assertThat(foundOrgUnit).isEqualTo(orgUnit);
    }

    @Test
    void getOrgUnitById_ShouldThrowException_WhenRoomDoesNotExist() {
        // Arrange: Stub the repository to return an empty result for a non-existent
        // orgUnit
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Attempt to retrieve the orgUnit and expect a
        // OrgUnitNotFoundException
        assertThrows(OrgUnitNotFoundException.class, () -> orgUnitService.getOrgUnitById(1L));
    }

    @Test
    void createOrgUnit_ShouldCreateOrgUnit_WhenRoomExists() {
        // Arrange: Stub room retrieval to return mockRoom when the specified ID
        // is used
        when(roomService.getRoomById(1L)).thenReturn(mockRoom);

        // Arrange: Prepare the OrgUnit DTO with the room ID as a string
        NewOrgUnitDTO orgUnitDTO = new NewOrgUnitDTO("New OrgUnit", "OrgUnit description", String.valueOf(1L), null);

        // Arrange: Create a mock OrgUnit that represents the saved orgUnit returned by
        // the repository
        OrgUnit mockOrgUnit = new OrgUnit(orgUnitDTO.getName(), orgUnitDTO.getDescription(), mockRoom);
        when(orgUnitRepository.save(any(OrgUnit.class))).thenReturn(mockOrgUnit);

        // Act: create a orgUnit using orgUnitService and pass in the orgUnit DTO
        OrgUnit createdOrgUnit = orgUnitService.createOrgUnit(orgUnitDTO);

        // Assert: verify that the created orgUnit is not null and matches the expected
        // details from orgUnitDTO
        assertThat(createdOrgUnit).isNotNull();
        assertThat(createdOrgUnit.getName()).isEqualTo(orgUnitDTO.getName());
        assertThat(createdOrgUnit.getDescription()).isEqualTo(orgUnitDTO.getDescription());
        assertThat(createdOrgUnit.getRoom()).isEqualTo(mockRoom);

        // Verify that orgUnitRepository.save() was called to persist the new orgUnit
        verify(orgUnitRepository).save(any(OrgUnit.class));
    }

    @Test
    void createOrgUnit_ShouldThrowException_WhenOrgUnitDoesNotExist() {
        // Arrange: Set up the DTO with a room ID that doesn't exist
        NewOrgUnitDTO orgUnitDTO = new NewOrgUnitDTO("New OrgUnit", "OrgUnit description", "999", null);
        when(roomService.getRoomById(orgUnitDTO.getRoomIdAsLong())).thenThrow(new RoomNotFoundException());

        // Act & Assert: Attempt to create the orgUnit and expect a
        // RoomNotFoundException
        assertThrows(RoomNotFoundException.class, () -> orgUnitService.createOrgUnit(orgUnitDTO));
    }

    @Disabled("Feature under development")
    @Test
    void createOrgUnit_ShouldThrowException_WhenOrgUnitLimitReached() {
        // Arrange: Set up a room with the maximum allowed orgUnits
        List<OrgUnit> orgUnits = new ArrayList<>();
        for (int i = 0; i < ORG_UNIT_LIMIT; i++) {
            orgUnits.add(new OrgUnit("OrgUnit " + (i + 1), "Description " + (i + 1), mockRoom));
        }
        mockRoom.setOrgUnits(orgUnits);

        // Stub the repository to return the room and orgUnits
        when(projectService.getProjectById(1L)).thenReturn(mockProject);
        when(orgUnitRepository.findByProjectId(1L)).thenReturn(orgUnits);

        NewOrgUnitDTO orgUnitDTO = new NewOrgUnitDTO("Extra OrgUnit", "Description", String.valueOf(1L), null);

        // Act & Assert: Attempt to create a orgUnit and expect an exception
        assertThrows(OrgUnitLimitReachedException.class, () -> orgUnitService.createOrgUnit(orgUnitDTO));
    }

    @Test
    void getUserOrgUnits_ShouldReturnOrgUnitsOwnedByUser() {
        // Arrange: Set up mock user, projects, and orgUnits, and stub the repository to
        // return orgUnits owned by the user
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        OrgUnit orgUnit1 = new OrgUnit("OrgUnit 1", "OrgUnit description 1", mockRoom);
        OrgUnit orgUnit2 = new OrgUnit("OrgUnit 2", "OrgUnit description 2", mockRoom);
        when(orgUnitRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(orgUnit1, orgUnit2));

        // Act: Retrieve the orgUnits owned by the user
        Iterable<OrgUnit> userOrgUnits = orgUnitService.getUserOrgUnits();

        // Assert: Verify that the result contains only the orgUnits owned by the user
        assertThat(userOrgUnits).containsExactly(orgUnit1, orgUnit2);
    }

    @Test
    void getUserOrgUnits_ShouldReturnOrgUnitsAcrossMultipleProjects() {
        // Arrange: Set up two projects for the same user with orgUnits
        Project project1 = new Project("Project 1", mockUser);
        Project project2 = new Project("Project 2", mockUser);

        Room room1 = new Room("Room 1", "Room Description 1", project1);
        Room room2 = new Room("Room 2", "Room Description 2", project2);

        OrgUnit orgUnit1 = new OrgUnit("OrgUnit 1", "Description 1", room1);
        OrgUnit orgUnit2 = new OrgUnit("OrgUnit 2", "Description 2", room2);

        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(orgUnitRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(orgUnit1, orgUnit2));

        // Act: Fetch orgUnits for the user
        Iterable<OrgUnit> userOrgUnits = orgUnitService.getUserOrgUnits();

        // Assert: Verify both orgUnits are returned across different projects
        assertThat(userOrgUnits).containsExactlyInAnyOrder(orgUnit1, orgUnit2);
    }

    @Test
    void getUserOrgUnits_ShouldReturnEmptyList_WhenNoOrgUnitsExist() {
        // Arrange: Set up mock user and stub the repository to return an empty list
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(orgUnitRepository.findByOwnerId(mockUser.getId())).thenReturn(Collections.emptyList());

        // Act: Retrieve the orgUnits owned by the user
        Iterable<OrgUnit> userOrgUnits = orgUnitService.getUserOrgUnits();

        // Assert: Verify that the result is empty
        assertThat(userOrgUnits).isEmpty();
    }

    @Test
    void updateOrgUnit_ShouldUpdateOrgUnit_WhenOrgUnitExists() {
        // Arrange: Set up mock orgUnit with initial values and stub the repository to
        // return the orgUnit by ID
        OrgUnit orgUnit = new OrgUnit("Old Name", "Old Description", mockRoom);
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(orgUnit));

        // Arrange: Create an UpdateOrgUnitDTO with updated values
        UpdateOrgUnitDTO orgUnitDTO = new UpdateOrgUnitDTO("Updated Name", "Updated Description");

        // Stub the repository to return the orgUnit after saving
        when(orgUnitRepository.save(orgUnit)).thenReturn(orgUnit);

        // Act: Update the orgUnit using the service
        OrgUnit updatedOrgUnit = orgUnitService.updateOrgUnit(1L, orgUnitDTO);

        // Assert: Verify that the orgUnit's name was updated correctly
        assertThat(updatedOrgUnit.getName()).isEqualTo("Updated Name");
        assertThat(updatedOrgUnit.getDescription()).isEqualTo("Updated Description");
        verify(orgUnitRepository).save(orgUnit);
    }

    @Test
    void updateOrgUnit_ShouldThrowException_WhenOrgUnitDoesNotExist() {
        // Arrange: Stub the repository to return an empty result when searching for a
        // non-existent orgUnit
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.empty());

        // Arrange: Set up an UpdateOrgUnitDTO with updated values
        UpdateOrgUnitDTO orgUnitDTO = new UpdateOrgUnitDTO("Updated Name", "Updated Description");

        // Act & Assert: Attempt to update the orgUnit and expect a
        // OrgUnitNotFoundException
        assertThrows(OrgUnitNotFoundException.class, () -> orgUnitService.updateOrgUnit(1L, orgUnitDTO));
    }

    @Test
    void updateOrgUnit_ShouldNotChangeDescription_WhenDescriptionIsNull() {
        // Arrange: Set up a orgUnit with an initial description
        OrgUnit orgUnit = new OrgUnit("OrgUnit Name", "Initial Description", mockRoom);
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(orgUnit));

        // Stub the repository to return the orgUnit after saving
        when(orgUnitRepository.save(orgUnit)).thenReturn(orgUnit);

        // Arrange: Set up an UpdateOrgUnitDTO with null description
        UpdateOrgUnitDTO orgUnitDTO = new UpdateOrgUnitDTO("Updated Name", null);

        // Act: Update orgUnit
        OrgUnit updatedOrgUnit = orgUnitService.updateOrgUnit(1L, orgUnitDTO);

        // Assert: Verify that the name was updated but the description remains the same
        assertThat(updatedOrgUnit.getName()).isEqualTo("Updated Name");
        assertThat(updatedOrgUnit.getDescription()).isEqualTo("Initial Description");
        verify(orgUnitRepository).save(orgUnit);
    }

    @Test
    void assignOrgUnitsToRoom_Success() {
        // Arrange: Set up target Room and mock org units to be assigned
        Room targetRoom = new Room("Target Room", "Description", mockProject);
        when(roomService.getRoomById(10L)).thenReturn(targetRoom);

        OrgUnit orgUnit1 = new OrgUnit("OrgUnit 1", "Description", mockProject);
        OrgUnit orgUnit2 = new OrgUnit("OrgUnit 2", "Description", mockProject);

        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(orgUnit1));
        when(orgUnitRepository.findById(2L)).thenReturn(Optional.of(orgUnit2));

        // Act: Assign org units to the target room
        Iterable<OrgUnit> assignedOrgUnits = orgUnitService.assignOrgUnitsToRoom(List.of(1L, 2L), 10L);

        // Assert: Verify org units are now associated with the target room
        assertThat(assignedOrgUnits).allMatch(orgUnit -> orgUnit.getRoom().equals(targetRoom));
    }

    // TODO allow partial success
    @Test
    void assignOrgUnitsToRoom_OrgUnitNotFound_ShouldThrowOrgUnitNotFoundException() {
        // Arrange: Set up target Room ID and a non-existent org unit ID
        Room targetRoom = new Room("Target Room", "Description", mockProject);
        Long nonExistentOrgUnitId = 999L;
        when(orgUnitRepository.findById(nonExistentOrgUnitId)).thenReturn(Optional.empty());

        // Act & Assert: Expect OrgUnitNotFoundException for the non-existent org unit
        assertThrows(OrgUnitNotFoundException.class, () -> {
            orgUnitService.assignOrgUnitsToRoom(List.of(nonExistentOrgUnitId), targetRoom.getId());
        });
    }

    // TODO allow partial success
    @Test
    void assignOrgUnitsToRoom_DifferentProject_ShouldThrowIllegalArgumentException() {
        // Arrange: Create org units with a different project than the target room
        Project differentProject = new Project("Different Project", mockUser);
        Room targetRoom = new Room("Target Room", "Description", mockProject);
        OrgUnit orgUnitWithDifferentProject = new OrgUnit("OrgUnit", "Description", differentProject);

        when(roomService.getRoomById(10L)).thenReturn(targetRoom);
        when(orgUnitRepository.findById(20L)).thenReturn(Optional.of(orgUnitWithDifferentProject));

        // Act & Assert: Expect IllegalArgumentException due to project mismatch
        assertThrows(IllegalArgumentException.class, () -> {
            orgUnitService.assignOrgUnitsToRoom(List.of(20L), 10L);
        });
    }

    @Test
    void unassignOrgUnitsFromRoom_Success() {
        // Arrange: Set up org units currently assigned to a room
        Room mockRoom = new Room("Mock Room", "Description", mockProject);
        OrgUnit orgUnit1 = new OrgUnit("OrgUnit 1", "Description", mockProject);
        OrgUnit orgUnit2 = new OrgUnit("OrgUnit 2", "Description", mockProject);
        orgUnit1.setRoom(mockRoom);
        orgUnit2.setRoom(mockRoom);

        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(orgUnit1));
        when(orgUnitRepository.findById(2L)).thenReturn(Optional.of(orgUnit2));

        // Act: Unassign the org units from the room
        Iterable<OrgUnit> unassignedOrgUnits = orgUnitService.unassignOrgUnits(List.of(1L, 2L));

        // Assert: Verify that each org unit is now unassigned
        for (OrgUnit orgUnit : unassignedOrgUnits) {
            assertThat(orgUnit.getRoom()).isNull();
        }
    }

    // when(orgUnitService.getOrgUnitById(999L)).thenThrow(new
    // OrgUnitNotFoundException());

    @Test
    void unassignOrgUnitsFromRoom_OrgUnitNotFound_ShouldThrowOrgUnitNotFoundException() {
        // Arrange: Set up org unit IDs, including a non-existent org unit ID
        List<Long> orgUnitIds = List.of(1L, 999L, 3L); // Assuming 999L does not exist
        OrgUnit orgUnit1 = new OrgUnit("Org Unit 1", "Description", mockProject);

        // Simulate OrgUnitNotFoundException for the non-existent org unit
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.of(orgUnit1));
        when(orgUnitRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert: Expect OrgUnitNotFoundException for the missing org unit
        assertThrows(OrgUnitNotFoundException.class, () -> {
            orgUnitService.unassignOrgUnits(orgUnitIds);
        });
    }

    @Test
    void deleteOrgUnit_ShouldDeleteOrgUnit_WhenOrgUnitExists() {
        // Arrange: Set up a orgUnit and stub the repository to return the orgUnit by ID
        OrgUnit orgUnit = new OrgUnit("Sample OrgUnit", "OrgUnit Description", mockRoom);
        Long orgUnitId = orgUnit.getId();
        when(orgUnitRepository.findById(orgUnitId)).thenReturn(Optional.of(orgUnit));

        // Act: Delete the orgUnit using the service
        orgUnitService.deleteOrgUnit(orgUnitId);

        // Assert: Verify that the repository's delete method was called with the
        // correct ID
        verify(orgUnitRepository).delete(orgUnit);
    }

    @Test
    void deleteOrgUnit_ShouldThrowException_WhenOrgUnitDoesNotExist() {
        // Arrange: Stub the repository to return an empty result when searching for a
        // non-existent orgUnit
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Attempt to delete the orgUnit and expect a
        // OrgUnitNotFoundException
        assertThrows(OrgUnitNotFoundException.class, () -> orgUnitService.deleteOrgUnit(1L));

        // Assert: Verify that the repository's delete method was never called
        verify(orgUnitRepository, never()).deleteById(anyLong());
    }
}
