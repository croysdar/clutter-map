package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.TestDataFactory;
import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.exception.room.RoomLimitReachedException;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.RoomRepository;
import app.cluttermap.util.ResourceType;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RoomServiceTests {
    @Mock
    private RoomRepository roomRepository;

    @Mock
    private OrgUnitRepository orgUnitRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private RoomService roomService;

    private User mockUser;
    private Project mockProject;

    private static int ROOM_LIMIT = 30;

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
        mockProject = new TestDataFactory.ProjectBuilder().user(mockUser).build();
    }

    @Test
    void getUserRooms_ShouldReturnRoomsOwnedByUser() {
        // Arrange: Set up mock user, projects, and rooms, and stub the repository to
        // return rooms owned by the user
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        Room room1 = new TestDataFactory.RoomBuilder().project(mockProject).build();
        Room room2 = new TestDataFactory.RoomBuilder().project(mockProject).build();
        when(roomRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(room1, room2));

        // Act: Retrieve the rooms owned by the user
        Iterable<Room> userRooms = roomService.getUserRooms();

        // Assert: Verify that the result contains only the rooms owned by the user
        assertThat(userRooms).containsExactly(room1, room2);
    }

    @Test
    void getUserRooms_ShouldReturnRoomsAcrossMultipleProjects() {
        // Arrange: Set up two projects for the same user with rooms
        Project project1 = new TestDataFactory.ProjectBuilder().user(mockUser).build();
        Project project2 = new TestDataFactory.ProjectBuilder().user(mockUser).build();

        Room room1 = new TestDataFactory.RoomBuilder().project(project1).build();
        Room room2 = new TestDataFactory.RoomBuilder().project(project2).build();

        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(roomRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(room1, room2));

        // Act: Fetch rooms for the user
        Iterable<Room> userRooms = roomService.getUserRooms();

        // Assert: Verify both rooms are returned across different projects
        assertThat(userRooms).containsExactlyInAnyOrder(room1, room2);
    }

    @Test
    void getUserRooms_ShouldReturnEmptyList_WhenNoRoomsExist() {
        // Arrange: Set up mock user and stub the repository to return an empty list
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(roomRepository.findByOwnerId(mockUser.getId())).thenReturn(Collections.emptyList());

        // Act: Retrieve the rooms owned by the user
        Iterable<Room> userRooms = roomService.getUserRooms();

        // Assert: Verify that the result is empty
        assertThat(userRooms).isEmpty();
    }

    @Test
    void getRoomId_ShouldReturnRoom_WhenRoomExists() {
        // Arrange: Set up a sample room and stub the repository to return it by ID
        Room room = new TestDataFactory.RoomBuilder().project(mockProject).build();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // Act: Retrieve the room using the service method
        Room foundRoom = roomService.getRoomById(1L);

        // Assert: Verify that the room retrieved matches the expected room
        assertThat(foundRoom).isEqualTo(room);
    }

    @Test
    void getRoomById_ShouldThrowException_WhenRoomDoesNotExist() {
        // Arrange: Stub the repository to return an empty result for a non-existent
        // room
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Attempt to retrieve the room and expect a RoomNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> roomService.getRoomById(1L));
    }

    @Test
    void createRoom_ShouldCreateRoom_WhenValid() {
        // Arrange: Stub project retrieval to return mockProject when the specified ID
        // is used
        when(projectService.getProjectById(1L)).thenReturn(mockProject);

        // Arrange: Prepare the Room DTO with the project ID as a string
        NewRoomDTO roomDTO = new TestDataFactory.NewRoomDTOBuilder().build();

        // Arrange: Create a Room that represents the saved room returned by the
        // repository
        Room room = new TestDataFactory.RoomBuilder().fromDTO(roomDTO).project(mockProject).build();
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        // Act: create a room using roomService and pass in the room DTO
        Room createdRoom = roomService.createRoom(roomDTO);

        // Assert: verify that the created room is not null and matches the expected
        // details from roomDTO
        assertThat(createdRoom).isNotNull();
        assertThat(createdRoom.getName()).isEqualTo(roomDTO.getName());
        assertThat(createdRoom.getDescription()).isEqualTo(roomDTO.getDescription());
        assertThat(createdRoom.getProject()).isEqualTo(mockProject);

        // Verify that roomRepository.save() was called to persist the new room
        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void createRoom_ShouldThrowException_WhenProjectDoesNotExist() {
        // Arrange: Set up the DTO with a project ID that doesn't exist
        NewRoomDTO roomDTO = new TestDataFactory.NewRoomDTOBuilder().projectId(999L).build();
        when(projectService.getProjectById(roomDTO.getProjectIdAsLong()))
                .thenThrow(new ResourceNotFoundException(ResourceType.PROJECT, 999L));

        // Act & Assert: Attempt to create the room and expect a
        // ProjectNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> roomService.createRoom(roomDTO));
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

    @Test
    void updateRoom_ShouldUpdateRoom_WhenRoomExists() {
        // Arrange: Set up mock room with initial values and stub the repository to
        // return the room by ID
        Room room = new TestDataFactory.RoomBuilder().name("Old Name").description("Old Description")
                .project(mockProject).build();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // Arrange: Create an UpdateRoomDTO with updated values
        UpdateRoomDTO roomDTO = new TestDataFactory.UpdateRoomDTOBuilder().build();

        // Stub the repository to return the room after saving
        when(roomRepository.save(room)).thenReturn(room);

        // Act: Update the room using the service
        Room updatedRoom = roomService.updateRoom(1L, roomDTO);

        // Assert: Verify that the room's name was updated correctly
        assertThat(updatedRoom.getName()).isEqualTo(roomDTO.getName());
        assertThat(updatedRoom.getDescription()).isEqualTo(roomDTO.getDescription());
        verify(roomRepository).save(room);
    }

    @Test
    void updateRoom_ShouldThrowException_WhenRoomDoesNotExist() {
        // Arrange: Stub the repository to return an empty result when searching for a
        // non-existent room
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Arrange: Set up an UpdateRoomDTO with updated values
        UpdateRoomDTO roomDTO = new TestDataFactory.UpdateRoomDTOBuilder().build();

        // Act & Assert: Attempt to update the room and expect a RoomNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> roomService.updateRoom(1L, roomDTO));
    }

    @Test
    void updateRoom_ShouldNotChangeDescription_WhenDescriptionIsNull() {
        // Arrange: Set up a room with an initial description
        Room room = new TestDataFactory.RoomBuilder().name("Old Name").description("Old Description")
                .project(mockProject).build();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // Stub the repository to return the room after saving
        when(roomRepository.save(room)).thenReturn(room);

        // Arrange: Set up an UpdateRoomDTO with null description
        UpdateRoomDTO roomDTO = new TestDataFactory.UpdateRoomDTOBuilder().description(null).build();

        // Act: Update room
        Room updatedRoom = roomService.updateRoom(1L, roomDTO);

        // Assert: Verify that the name was updated but the description remains the same
        assertThat(updatedRoom.getName()).isEqualTo(roomDTO.getName());
        assertThat(updatedRoom.getDescription()).isEqualTo("Old Description");
        verify(roomRepository).save(room);
    }

    @Test
    void deleteRoom_ShouldDeleteRoom_WhenRoomExists() {
        // Arrange: Set up a room and stub the repository to return the room by ID
        Room room = new TestDataFactory.RoomBuilder().project(mockProject).build();
        Long roomId = room.getId();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // Act: Delete the room using the service
        roomService.deleteRoomById(roomId);

        // Assert: Verify that the repository's delete method was called with the
        // correct ID
        verify(roomRepository).delete(room);
    }

    @Test
    void deleteRoom_ShouldThrowException_WhenRoomDoesNotExist() {
        // Arrange: Stub the repository to return an empty result when searching for a
        // non-existent room
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Attempt to delete the room and expect a RoomNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> roomService.deleteRoomById(1L));

        // Assert: Verify that the repository's delete method was never called
        verify(roomRepository, never()).deleteById(anyLong());
    }
}
