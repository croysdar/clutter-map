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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import app.cluttermap.TestDataFactory;
import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.exception.room.RoomLimitReachedException;
import app.cluttermap.model.Event;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;
import app.cluttermap.repository.RoomRepository;
import app.cluttermap.util.ResourceType;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RoomServiceTests {
    @Mock
    private RoomRepository roomRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private ProjectService projectService;

    @Mock
    private EventService eventService;

    @InjectMocks
    private RoomService roomService;

    private User mockUser;
    private Project mockProject;

    private static int ROOM_LIMIT = 30;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(roomService, "self", roomService);

        mockUser = new User("mockProviderId");
        mockProject = new TestDataFactory.ProjectBuilder().user(mockUser).build();
    }

    @Test
    void getUserRooms_ShouldReturnRoomsOwnedByUser() {
        // Arrange: Mock the current user and rooms
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        Room room1 = new TestDataFactory.RoomBuilder().project(mockProject).build();
        Room room2 = new TestDataFactory.RoomBuilder().project(mockProject).build();
        when(roomRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(room1, room2));

        // Act: Call service method
        Iterable<Room> userRooms = roomService.getUserRooms();

        // Assert: Verify the result contains the expected rooms
        assertThat(userRooms).containsExactly(room1, room2)
                .as("Rooms owned by user should be returned when they exist");

        // Verify dependencies are called as expected
        verify(securityService).getCurrentUser();
        verify(roomRepository).findByOwnerId(mockUser.getId());
    }

    @Test
    void getUserRooms_ShouldReturnEmptyList_WhenNoRoomsExist() {
        // Arrange: Mock the current user and an empty repository result
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(roomRepository.findByOwnerId(mockUser.getId())).thenReturn(Collections.emptyList());

        // Act: Call service method
        Iterable<Room> userRooms = roomService.getUserRooms();

        // Assert: Verify that the result is empty
        assertThat(userRooms)
                .as("Empty list should be returned when user owns no rooms")
                .isEmpty();

        // Verify dependencies are called as expected
        verify(securityService).getCurrentUser();
        verify(roomRepository).findByOwnerId(mockUser.getId());
    }

    @ParameterizedTest
    @CsvSource({
            "true, Room should be returned when it exists",
            "false, ResourceNotFoundException should be thrown when room does not exist"
    })
    void getRoomById_ShouldHandleExistenceCorrectly(boolean roomExists, String description) {
        // Arrange
        Long resourceId = 1L;
        if (roomExists) {
            // Arrange: Mock the repository to return an room
            Room mockRoom = mockRoomInRepository(resourceId);

            // Act: Call service method
            Room foundRoom = roomService.getRoomById(resourceId);

            // Assert: Verify the room retrieved matches the mock
            assertThat(foundRoom)
                    .as(description)
                    .isNotNull()
                    .isEqualTo(mockRoom);

        } else {
            // Arrange: Mock the repository to return empty
            mockNonexistentRoomInRepository(resourceId);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> roomService.getRoomById(resourceId),
                    description);
        }

        // Verify: Ensure repository interaction occurred
        verify(roomRepository).findById(anyLong());
    }

    @Test
    void createRoom_ShouldCreateRoom_WhenValid() {
        mockProjectLookup();

        // Arrange: Prepare the Room DTO with the project ID as a string
        NewRoomDTO roomDTO = new TestDataFactory.NewRoomDTOBuilder().build();

        // Arrange: Create a Room that represents the saved room returned by the
        // repository
        Room mockRoom = new TestDataFactory.RoomBuilder().fromDTO(roomDTO).project(mockProject).build();
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);

        // Arrange: Mock event logging
        mockLogCreateEvent();

        // Act: Call the service method
        Room createdRoom = roomService.createRoom(roomDTO);

        // Assert: Validate the created room
        assertThat(createdRoom)
                .as("Room should be created when valid")
                .isNotNull()
                .isEqualTo(mockRoom);

        // Verify that the correct service and repository methods were called
        verify(projectService).getProjectById(mockProject.getId());
        verify(roomRepository).save(any(Room.class));

        // Capture and verify the arguments passed to logUpdateEvent
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        // Assert: Verify event logging
        verify(eventService).logCreateEvent(eq(ResourceType.ROOM), eq(mockRoom.getId()),
                payloadCaptor.capture());

        // Assert: Verify the payload contains the expected values
        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload)
                .containsEntry("name", createdRoom.getName());
        assertThat(capturedPayload)
                .containsEntry("description", createdRoom.getDescription());
    }

    @Disabled("Feature under development")
    @Test
    void createRoom_ShouldThrowException_WhenRoomLimitReached() {
        // Arrange: Set up a project with the maximum allowed rooms
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < ROOM_LIMIT; i++) {
            rooms.add(new TestDataFactory.RoomBuilder().project(mockProject).build());
        }
        mockProject.setRooms(rooms);

        // Stub the repository to return the project and rooms
        when(projectService.getProjectById(1L)).thenReturn(mockProject);
        when(roomRepository.findByProjectId(1L)).thenReturn(rooms);

        NewRoomDTO roomDTO = new TestDataFactory.NewRoomDTOBuilder().build();

        // Act & Assert: Attempt to create a room and expect an exception
        assertThrows(RoomLimitReachedException.class, () -> roomService.createRoom(roomDTO));
    }

    @ParameterizedTest
    @CsvSource({
            "true, All fields should update when all fields are provided",
            "false, Description should not update when it is null",
    })
    void updateRoom_ShouldUpdateFieldsConditionally(
            boolean updateDescription, String description) {
        // Arrange: Set up mock room with initial values
        Long resourceId = 1L;
        String oldDescription = "Old Description";

        Room room = new TestDataFactory.RoomBuilder()
                .name("Old Name")
                .description(oldDescription)
                .project(mockProject).build();

        when(roomRepository.findById(resourceId)).thenReturn(Optional.of(room));

        // Arrange: Use conditional variables for the expected values of the updated
        // fields
        String newDescription = updateDescription ? "New Description" : null;

        // Build the UpdateRoomDTO with these values
        UpdateRoomDTO roomDTO = new TestDataFactory.UpdateRoomDTOBuilder()
                .description(newDescription)
                .build();

        // Stub the repository to return the room after saving
        when(roomRepository.save(room)).thenReturn(room);

        // Arrange: Mock event logging
        mockLogUpdateEvent();

        // Act: Call the service method
        roomService.updateRoom(resourceId, roomDTO);

        // Capture the saved room to verify fields
        ArgumentCaptor<Room> savedRoomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(savedRoomCaptor.capture());
        Room savedRoom = savedRoomCaptor.getValue();

        // Assert: Verify the fields were updated as expected
        assertThat(savedRoom.getName())
                .as(description + ": Name should match the DTO name")
                .isEqualTo(roomDTO.getName());
        assertThat(savedRoom.getDescription())
                .as(description + ": Description should match the expected value")
                .isEqualTo(updateDescription ? newDescription : oldDescription);

        // Capture and verify the arguments passed to logUpdateEvent
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        // Verify the event was logged
        verify(eventService).logUpdateEvent(
                eq(ResourceType.ROOM),
                eq(resourceId),
                payloadCaptor.capture());

        // Assert: Verify the payload contains the expected changes
        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload)
                .containsEntry("name", savedRoom.getName());
        if (updateDescription) {
            assertThat(capturedPayload)
                    .containsEntry("description", savedRoom.getDescription());
        } else {
            assertThat(capturedPayload)
                    .doesNotContainEntry("description", savedRoom.getDescription());
        }
    }

    @Test
    void updateRoom_ShouldThrowException_WhenRoomDoesNotExist() {
        // Arrange: Stub the repository to return an empty result when searching for a
        // non-existent room
        mockNonexistentRoomInRepository(999L);

        // Arrange: Set up an UpdateRoomDTO
        UpdateRoomDTO roomDTO = new TestDataFactory.UpdateRoomDTOBuilder().build();

        // Act & Assert: Expect ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> roomService.updateRoom(999L, roomDTO));

        // Verify: Ensure save was not called
        verify(roomRepository, never()).save(any(Room.class));
    }

    @ParameterizedTest
    @CsvSource({
            "true, Room should be deleted when it exists",
            "false, Exception should be thrown when room does not exist"
    })
    void deleteRoom_ShouldHandleExistenceCorrectly(boolean roomExists, String description) {
        Long resourceId = 1L;
        if (roomExists) {
            // Arrange: Stub the repository to simulate finding room
            mockRoomInRepository(resourceId);

            // Arrange: Mock event logging
            mockLogDeleteEvent();

            // Act: Call the service method
            roomService.deleteRoomById(resourceId);

            // Assert: Verify that the repository's delete method was called with the
            // correct ID
            verify(roomRepository).delete(any(Room.class));

            verify(eventService).logDeleteEvent(
                    eq(ResourceType.ROOM),
                    eq(resourceId));
        } else {
            // Arrange: Stub the repository to simulate not finding room
            mockNonexistentRoomInRepository(resourceId);

            // Act & Assert: Attempt to delete the room and expect a
            // ResourceNotFoundException
            assertThrows(ResourceNotFoundException.class, () -> roomService.deleteRoomById(resourceId));

            // Assert: Verify that the repository's delete method was never called
            verify(roomRepository, never()).delete(any(Room.class));
        }
    }

    private void mockNonexistentRoomInRepository(Long resourceId) {
        when(roomRepository.findById(resourceId)).thenReturn(Optional.empty());
    }

    private Room mockRoomInRepository(Long resourceId) {
        Room mockRoom = new TestDataFactory.RoomBuilder().id(resourceId).project(mockProject).build();
        when(roomRepository.findById(resourceId)).thenReturn(Optional.of(mockRoom));
        return mockRoom;
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
