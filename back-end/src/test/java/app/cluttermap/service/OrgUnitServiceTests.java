package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import app.cluttermap.TestDataFactory;
import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.exception.org_unit.OrgUnitLimitReachedException;
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

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class OrgUnitServiceTests {
    @Mock
    private OrgUnitRepository orgUnitRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private ProjectService projectService;

    @Mock
    private RoomService roomService;

    @Mock
    private EventService eventService;

    @InjectMocks
    private OrgUnitService orgUnitService;

    private User mockUser;
    private Project mockProject;
    private Room mockRoom;

    private static int ORG_UNIT_LIMIT = 100;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orgUnitService, "self", orgUnitService);

        mockUser = new User("mockProviderId");

        mockProject = new TestDataFactory.ProjectBuilder().user(mockUser).build();

        mockRoom = new TestDataFactory.RoomBuilder().project(mockProject).build();
    }

    @Test
    void getUserOrgUnits_ShouldReturnOrgUnitsOwnedByUser() {
        // Arrange: Mock the current user and org units
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        OrgUnit orgUnit1 = new TestDataFactory.OrgUnitBuilder().room(mockRoom).build();
        OrgUnit orgUnit2 = new TestDataFactory.OrgUnitBuilder().room(mockRoom).build();
        when(orgUnitRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(orgUnit1, orgUnit2));

        // Act: Call service method
        Iterable<OrgUnit> userOrgUnits = orgUnitService.getUserOrgUnits();

        // Assert: Verify that the result contains only the orgUnits owned by the user
        assertThat(userOrgUnits).containsExactly(orgUnit1, orgUnit2)
                .as("Org Units owned by user should be returned when they exist");

        // Verify dependencies are called as expected
        verify(securityService).getCurrentUser();
        verify(orgUnitRepository).findByOwnerId(mockUser.getId());
    }

    @Test
    void getUserOrgUnits_ShouldReturnEmptyList_WhenNoOrgUnitsExist() {
        // Arrange: Mock the current user and an empty repository result
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(orgUnitRepository.findByOwnerId(mockUser.getId())).thenReturn(Collections.emptyList());

        // Act: Call service method
        Iterable<OrgUnit> userOrgUnits = orgUnitService.getUserOrgUnits();

        // Assert: Verify that the result is empty
        assertThat(userOrgUnits)
                .as("Empty list should be returned when user owns no org units")
                .isEmpty();

        // Verify dependencies are called as expected
        verify(securityService).getCurrentUser();
        verify(orgUnitRepository).findByOwnerId(mockUser.getId());
    }

    @ParameterizedTest
    @CsvSource({
            "true, Org Unit should be returned when it exists",
            "false, ResourceNotFoundException should be thrown when Org Unit does not exist"
    })
    void getOrgUnitById_ShouldReturnHandleExistenceCorrectly(boolean orgUnitExists, String description) {
        // Arrange
        Long resourceId = 1L;
        if (orgUnitExists) {
            // Arrange: Mock the repository to return an org unit
            OrgUnit mockOrgUnit = mockAssignedOrgUnitInRepository(resourceId);

            // Act: Call service method
            OrgUnit foundOrgUnit = orgUnitService.getOrgUnitById(resourceId);

            // Assert: Verify the org unit retrieved matches the mock
            assertThat(foundOrgUnit)
                    .as(description)
                    .isNotNull()
                    .isEqualTo(mockOrgUnit);

        } else {
            // Arrange: Mock the repository to return empty
            mockNonexistentOrgUnitInRepository(resourceId);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> orgUnitService.getOrgUnitById(resourceId),
                    description);
        }

        // Verify: Ensure repository interaction occurred
        verify(orgUnitRepository).findById(anyLong());
    }

    @ParameterizedTest
    @CsvSource({
            "true, Unassigned org unit should be returned when the project has unassigned org units",
            "false, Empty list should be returned when the project has no unassigned org units"
    })
    void getUnassignedOrgUnitsByProjectId_ShouldReturnCorrectOrgUnits(boolean unassignedOrgUnitsExist,
            String description) {
        // Arrange: Prepare mock data
        List<OrgUnit> mockOrgUnits = unassignedOrgUnitsExist
                ? List.of(new TestDataFactory.OrgUnitBuilder().project(mockProject).build())
                : List.of();

        Long projectId = mockProject.getId();
        when(orgUnitRepository.findUnassignedOrgUnitsByProjectId(projectId)).thenReturn(mockOrgUnits);

        // Act: Call the service method
        List<OrgUnit> unassignedOrgUnits = orgUnitService.getUnassignedOrgUnitsByProjectId(projectId);

        // Assert: Verify the result
        assertThat(unassignedOrgUnits)
                .as(description)
                .isEqualTo(mockOrgUnits);

        // Verify: Ensure repository interaction occurred
        verify(orgUnitRepository).findUnassignedOrgUnitsByProjectId(projectId);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Org Unit should be created in the room when roomId is provided",
            "false, Org Unit should be created as unassigned when projectId is provided and orgUnitId is null"
    })
    void createOrgUnit_ShouldCreateOrgUnit(boolean isRoomProvided, String description) {
        Long roomId = isRoomProvided ? mockRoom.getId() : null;
        Long projectId = isRoomProvided ? null : mockProject.getId();

        // Arrange: Prepare the OrgUnit DTO based on the parameters
        NewOrgUnitDTO orgUnitDTO = new TestDataFactory.NewOrgUnitDTOBuilder()
                .roomId(roomId)
                .projectId(projectId)
                .build();

        OrgUnit mockOrgUnit;
        if (isRoomProvided) {
            // Mock room and corresponding org unit
            mockRoomLookup();
            mockOrgUnit = new TestDataFactory.OrgUnitBuilder().fromDTO(orgUnitDTO).room(mockRoom).build();
        } else {
            // Mock project and corresponding org unit
            mockProjectLookup();
            mockOrgUnit = new TestDataFactory.OrgUnitBuilder().fromDTO(orgUnitDTO).project(mockProject).build();
        }

        when(orgUnitRepository.save(any(OrgUnit.class))).thenReturn(mockOrgUnit);

        // Arrange: Mock event logging
        mockLogCreateEvent();

        // Act: Call the service method
        OrgUnit createdOrgUnit = orgUnitService.createOrgUnit(orgUnitDTO);

        // Assert: Validate the created org unit
        assertThat(createdOrgUnit)
                .as(description)
                .isNotNull()
                .isEqualTo(mockOrgUnit);

        // Verify that the correct service and repository methods were called
        if (isRoomProvided) {
            verify(roomService).getRoomById(roomId);
        } else {
            verify(projectService).getProjectById(projectId);
        }
        verify(orgUnitRepository).save(any(OrgUnit.class));

        // Capture and verify the arguments passed to logUpdateEvent
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        // Assert: Verify event logging
        verify(eventService).logCreateEvent(eq(ResourceType.ORGANIZATIONAL_UNIT), eq(mockOrgUnit.getId()),
                payloadCaptor.capture());

        // Assert: Verify the payload contains the expected values
        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload)
                .containsEntry("name", createdOrgUnit.getName());
        assertThat(capturedPayload)
                .containsEntry("description", createdOrgUnit.getDescription());
        if (isRoomProvided) {
            assertThat(capturedPayload)
                    .containsEntry("roomId", createdOrgUnit.getRoomId());
        } else {
            assertThat(capturedPayload)
                    .doesNotContainEntry("roomId", createdOrgUnit.getRoomId());
        }
    }

    @Disabled("Feature under development")
    @Test
    void createOrgUnit_ShouldThrowException_WhenOrgUnitLimitReached() {
        // Arrange: Set up a room with the maximum allowed orgUnits
        List<OrgUnit> orgUnits = new ArrayList<>();
        for (int i = 0; i < ORG_UNIT_LIMIT; i++) {
            orgUnits.add(new TestDataFactory.OrgUnitBuilder().room(mockRoom).build());
        }
        mockRoom.setOrgUnits(orgUnits);

        // Stub the repository to return the room and orgUnits
        mockProjectLookup();
        when(orgUnitRepository.findByProjectId(1L)).thenReturn(orgUnits);

        NewOrgUnitDTO orgUnitDTO = new TestDataFactory.NewOrgUnitDTOBuilder().build();

        // Act & Assert: Attempt to create a orgUnit and expect an exception
        assertThrows(OrgUnitLimitReachedException.class, () -> orgUnitService.createOrgUnit(orgUnitDTO));
    }

    @ParameterizedTest
    @CsvSource({
            "true, All fields should update when all fields are provided",
            "false, Description should not update when it is null",
    })
    void updateOrgUnit_ShouldUpdateFieldsConditionally(
            boolean updateDescription, String description) {
        // Arrange: Set up mock org unit with initial values
        Long resourceId = 1L;
        String oldDescription = "Old Description";

        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder()
                .name("Old Name")
                .description(oldDescription)
                .room(mockRoom).build();

        when(orgUnitRepository.findById(resourceId)).thenReturn(Optional.of(orgUnit));

        // Arrange: Use conditional variables for the expected values of the updated
        // fields
        String newDescription = updateDescription ? "New Description" : null;

        // Build the UpdateOrgUnitDTO with these values
        UpdateOrgUnitDTO orgUnitDTO = new TestDataFactory.UpdateOrgUnitDTOBuilder()
                .name("New Name")
                .description(newDescription)
                .build();

        // Stub the repository to return the org unit after saving
        when(orgUnitRepository.save(orgUnit)).thenReturn(orgUnit);

        // Arrange: Mock event logging
        mockLogUpdateEvent();

        // Act: Call the service method
        orgUnitService.updateOrgUnit(resourceId, orgUnitDTO);

        // Capture the saved org unit to verify fields
        ArgumentCaptor<OrgUnit> savedOrgUnitCaptor = ArgumentCaptor.forClass(OrgUnit.class);
        verify(orgUnitRepository).save(savedOrgUnitCaptor.capture());
        OrgUnit savedOrgUnit = savedOrgUnitCaptor.getValue();

        // Assert: Verify the fields were updated as expected
        assertThat(savedOrgUnit.getName())
                .as(description + ": Name should match the DTO name")
                .isEqualTo(orgUnitDTO.getName());
        assertThat(savedOrgUnit.getDescription())
                .as(description + ": Description should match the expected value")
                .isEqualTo(updateDescription ? newDescription : oldDescription);

        // Capture and verify the arguments passed to logUpdateEvent
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        // Verify the event was logged
        verify(eventService).logUpdateEvent(
                eq(ResourceType.ORGANIZATIONAL_UNIT),
                eq(resourceId),
                payloadCaptor.capture());

        // Assert: Verify the payload contains the expected changes
        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload)
                .containsEntry("name", savedOrgUnit.getName());
        if (updateDescription) {
            assertThat(capturedPayload)
                    .containsEntry("description", savedOrgUnit.getDescription());
        } else {
            assertThat(capturedPayload)
                    .doesNotContainEntry("description", savedOrgUnit.getDescription());
        }
    }

    @Test
    void updateOrgUnit_ShouldThrowException_WhenOrgUnitDoesNotExist() {
        // Arrange: Stub the repository to return an empty result for non-existent org
        // unit
        mockNonexistentOrgUnitInRepository(999L);

        // Arrange: Set up an UpdateOrgUnitDTO
        UpdateOrgUnitDTO orgUnitDTO = new TestDataFactory.UpdateOrgUnitDTOBuilder().build();

        // Act & Assert: Expect ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class,
                () -> orgUnitService.updateOrgUnit(999L, orgUnitDTO));

        // Verify: Ensure save was not called
        verify(orgUnitRepository, never()).save(any(OrgUnit.class));
    }

    @ParameterizedTest
    @CsvSource({
            "true, Org Unit should be deleted when it exists",
            "false, Exception should be thrown when org unit does not exist"
    })
    void deleteOrgUnit_ShouldHandleExistenceCorrectly(boolean orgUnitExists, String description) {
        Long resourceId = 1L;
        if (orgUnitExists) {
            // Arrange: Stub the repository to simulate finding org unit
            mockAssignedOrgUnitInRepository(resourceId);

            // Arrange: Mock event logging
            mockLogDeleteEvent();

            // Act: Call the service method
            orgUnitService.deleteOrgUnitById(resourceId);

            // Assert: Verify that the repository's delete method was called with the
            // correct ID
            verify(orgUnitRepository).delete(any(OrgUnit.class));

            // Verify the event was logged
            verify(eventService).logDeleteEvent(
                    eq(ResourceType.ORGANIZATIONAL_UNIT),
                    eq(resourceId));
        } else {
            // Arrange: Stub the repository to simulate not finding org unit
            mockNonexistentOrgUnitInRepository(resourceId);

            // Act & Assert: Attempt to delete the org unit and expect a
            // ResourceNotFoundException
            assertThrows(ResourceNotFoundException.class, () -> orgUnitService.deleteOrgUnitById(resourceId));

            // Assert: Verify that the repository's delete method was never called
            verify(orgUnitRepository, never()).delete(any(OrgUnit.class));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "true, Org Unit should be successfully assigned to the room",
            "false, ResourceNotFoundException should be thrown when org unit does not exist"
    })
    void assignOrgUnitsToRoom_ShouldHandleExistenceCorrectly(boolean orgUnitExists, String description) {
        // Arrange: Use the existing mockRoom as the target
        mockRoomLookup();

        // Arrange: Mock org units to move
        Long resourceId = 1L;
        if (orgUnitExists) {
            mockUnassignedOrgUnitInRepository(resourceId);
            // Act: Call the service method
            Iterable<OrgUnit> movedOrgUnits = orgUnitService.assignOrgUnitsToRoom(List.of(resourceId),
                    mockRoom.getId());

            // Assert: Verify that the org unit is assigned to the target room
            assertThat(movedOrgUnits)
                    .as(description)
                    .allMatch(orgUnit -> orgUnit.getRoom().equals(mockRoom));

            // Verify interactions
            verify(orgUnitRepository).findById(resourceId);
            verify(roomRepository).save(any(Room.class));
        } else {
            mockNonexistentOrgUnitInRepository(resourceId);
            // Assert: Expect ResourceNotFoundException
            assertThrows(ResourceNotFoundException.class,
                    () -> orgUnitService.assignOrgUnitsToRoom(List.of(resourceId), mockRoom.getId()),
                    description);

            // Verify no interactions with the repository save method
            verify(orgUnitRepository, never()).save(any(OrgUnit.class));
        }
    }

    @Test
    void assignOrgUnitsToRoom_ShouldHandleAssignedAndUnassignedOrgUnits() {
        // Arrange: Use the existing mockRoom as the target
        mockRoomLookup();

        // Mock unassigned org unit
        OrgUnit unassignedOrgUnit1 = mockUnassignedOrgUnitInRepository(1L);

        // Mock a previously assigned org unit
        Room previousRoom = new TestDataFactory.RoomBuilder().id(10L).project(mockProject).build();
        OrgUnit assignedOrgUnit = mockAssignedOrgUnitInRepository(3L, previousRoom);

        // Act: Assign multiple org units
        Iterable<OrgUnit> movedOrgUnits = orgUnitService.assignOrgUnitsToRoom(
                List.of(unassignedOrgUnit1.getId(), assignedOrgUnit.getId()),
                mockRoom.getId());

        // Assert: Verify all org units are assigned to the target room
        assertThat(movedOrgUnits).allMatch(orgUnit -> orgUnit.getRoom().equals(mockRoom));

        // Verify unassignment and reassignment for the previously assigned org unit
        verify(roomRepository).save(previousRoom); // Unassign
        verify(roomRepository, times(2)).save(mockRoom); // Assign all org units
        verify(orgUnitRepository, times(2)).findById(anyLong());

        // Verify logUpdateEvent is called for all org units
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        verify(eventService, times(2)).logUpdateEvent(
                eq(ResourceType.ORGANIZATIONAL_UNIT),
                anyLong(),
                payloadCaptor.capture());

        // Assert: Verify the payloads contain the expected changes
        List<Map<String, Object>> capturedPayloads = payloadCaptor.getAllValues();

        assertThat(capturedPayloads).hasSize(2);

        // Verify payload for the first item
        assertThat(capturedPayloads.get(0))
                .containsEntry("roomId", mockRoom.getId());

        // Verify payload for the second item
        assertThat(capturedPayloads.get(1))
                .containsEntry("roomId", mockRoom.getId());
    }

    @Test
    void assignOrgUnitsToRoom_DifferentProject_ShouldThrowIllegalArgumentException() {
        // Arrange: Use the existing mockRoom as the target
        mockRoomLookup();

        // Mock an org unit from a different project
        Project differentProject = new TestDataFactory.ProjectBuilder().id(2L).user(mockUser).build();
        OrgUnit orgUnitWithDifferentProject = new TestDataFactory.OrgUnitBuilder().project(differentProject).build();
        when(orgUnitRepository.findById(2L)).thenReturn(Optional.of(orgUnitWithDifferentProject));

        // Act & Assert: Expect IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> orgUnitService.assignOrgUnitsToRoom(List.of(2L), mockRoom.getId()),
                "Should throw IllegalArgumentException for org units from different projects");

        // Verify: Ensure no repository updates occurred
        verify(orgUnitRepository, never()).save(any(OrgUnit.class));
    }

    @Test
    void unassignOrgUnits_Success() {
        // Arrange: Mock org units and associate them with the room
        OrgUnit orgUnit1 = mockAssignedOrgUnitInRepository(1L);
        OrgUnit orgUnit2 = mockAssignedOrgUnitInRepository(2L);

        // Act: Call the service method
        Iterable<OrgUnit> unassignedOrgUnits = orgUnitService
                .unassignOrgUnits(List.of(orgUnit1.getId(), orgUnit2.getId()));

        // Assert: Verify that each org unit is now unassigned
        unassignedOrgUnits.forEach(orgUnit -> assertThat(orgUnit.getRoom()).isNull());

        // Verify repository interactions
        verify(orgUnitRepository, times(2)).findById(anyLong());
        verify(roomRepository, times(2)).save(any(Room.class));

        // Verify logUpdateEvent is called for both items
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        verify(eventService, times(2)).logUpdateEvent(
                eq(ResourceType.ORGANIZATIONAL_UNIT),
                anyLong(),
                payloadCaptor.capture());

        // Assert: Verify the payloads contain the expected changes
        List<Map<String, Object>> capturedPayloads = payloadCaptor.getAllValues();

        assertThat(capturedPayloads).hasSize(2);

        // Verify payload for the first item
        assertThat(capturedPayloads.get(0))
                .containsEntry("roomId", null);

        // Verify payload for the second item
        assertThat(capturedPayloads.get(1))
                .containsEntry("roomId", null);
    }

    @Test
    void unassignOrgUnitsFromRoom_OrgUnitNotFound_ShouldThrowOrgUnitNotFoundException() {
        // Arrange: Set up a non-existent orgUnit ID
        Long nonExistentOrgUnitId = 999L;
        mockNonexistentOrgUnitInRepository(nonExistentOrgUnitId);

        // Act & Assert: Expect ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            orgUnitService.unassignOrgUnits(List.of(nonExistentOrgUnitId));
        });

        // Verify: Ensure save was never called
        verify(orgUnitRepository, never()).save(any(OrgUnit.class));
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUserDoesNotOwnOrgUnit() {
        // Overwrite the default stub for `isResourceOwner` to deny access to the org
        // unit
        List<Long> orgUnitIds = List.of(1L, 2L, 3L);
        when(securityService.isResourceOwner(anyLong(), eq(ResourceType.ORGANIZATIONAL_UNIT))).thenReturn(false);

        // Act & Assert:
        assertThrows(AccessDeniedException.class, () -> {
            orgUnitService.checkOwnershipForOrgUnits(orgUnitIds);
        });
    }

    private void mockNonexistentOrgUnitInRepository(Long resourceId) {
        when(orgUnitRepository.findById(resourceId)).thenReturn(Optional.empty());
    }

    private OrgUnit mockAssignedOrgUnitInRepository(Long resourceId) {
        OrgUnit mockOrgUnit = new TestDataFactory.OrgUnitBuilder().id(resourceId).room(mockRoom).build();
        when(orgUnitRepository.findById(resourceId)).thenReturn(Optional.of(mockOrgUnit));
        return mockOrgUnit;
    }

    private OrgUnit mockAssignedOrgUnitInRepository(Long resourceId, Room room) {
        OrgUnit mockOrgUnit = new TestDataFactory.OrgUnitBuilder().id(resourceId).room(room).build();
        when(orgUnitRepository.findById(resourceId)).thenReturn(Optional.of(mockOrgUnit));
        return mockOrgUnit;
    }

    private OrgUnit mockUnassignedOrgUnitInRepository(Long resourceId) {
        OrgUnit mockOrgUnit = new TestDataFactory.OrgUnitBuilder().id(resourceId).project(mockProject).build();
        when(orgUnitRepository.findById(resourceId)).thenReturn(Optional.of(mockOrgUnit));
        return mockOrgUnit;
    }

    private void mockRoomLookup() {
        when(roomService.getRoomById(mockRoom.getId())).thenReturn(mockRoom);
    }

    private void mockProjectLookup() {
        when(projectService.getProjectById(mockProject.getId())).thenReturn(mockProject);
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
