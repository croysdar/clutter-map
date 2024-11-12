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

import app.cluttermap.exception.org_unit.OrgUnitNotFoundException;
import app.cluttermap.exception.project.ProjectNotFoundException;
import app.cluttermap.exception.room.RoomLimitReachedException;
import app.cluttermap.exception.room.RoomNotFoundException;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.RoomRepository;

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
        mockProject = new Project("Mock Project", mockUser);
    }

    @Test
    void getRoomId_ShouldReturnRoom_WhenRoomExists() {
        // Arrange: Set up a sample room and stub the repository to return it by ID
        Room room = new Room("Sample Room", "This is a room", mockProject);
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
        assertThrows(RoomNotFoundException.class, () -> roomService.getRoomById(1L));
    }

    @Test
    void createRoom_ShouldCreateRoom_WhenProjectExists() {
        // Arrange: Stub project retrieval to return mockProject when the specified ID
        // is used
        when(projectService.getProjectById(1L)).thenReturn(mockProject);

        // Arrange: Prepare the Room DTO with the project ID as a string
        NewRoomDTO roomDTO = new NewRoomDTO("New Room", "Room description", String.valueOf(1L));

        // Arrange: Create a mock Room that represents the saved room returned by the
        // repository
        Room mockRoom = new Room(roomDTO.getName(), roomDTO.getDescription(), mockProject);
        when(roomRepository.save(any(Room.class))).thenReturn(mockRoom);

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
        NewRoomDTO roomDTO = new NewRoomDTO("New Room", "Room description", "999");
        when(projectService.getProjectById(roomDTO.getProjectIdAsLong())).thenThrow(new ProjectNotFoundException());

        // Act & Assert: Attempt to create the room and expect a
        // ProjectNotFoundException
        assertThrows(ProjectNotFoundException.class, () -> roomService.createRoom(roomDTO));
    }

    @Disabled("Feature under development")
    @Test
    void createRoom_ShouldThrowException_WhenRoomLimitReached() {
        // Arrange: Set up a project with the maximum allowed rooms
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < ROOM_LIMIT; i++) {
            rooms.add(new Room("Room " + (i + 1), "Description " + (i + 1), mockProject));
        }
        mockProject.setRooms(rooms);

        // Stub the repository to return the project and rooms
        when(projectService.getProjectById(1L)).thenReturn(mockProject);
        when(roomRepository.findByProjectId(1L)).thenReturn(rooms);

        NewRoomDTO roomDTO = new NewRoomDTO("Extra Room", "Description", String.valueOf(1L));

        // Act & Assert: Attempt to create a room and expect an exception
        assertThrows(RoomLimitReachedException.class, () -> roomService.createRoom(roomDTO));
    }

    @Test
    void getUserRooms_ShouldReturnRoomsOwnedByUser() {
        // Arrange: Set up mock user, projects, and rooms, and stub the repository to
        // return rooms owned by the user
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        Room room1 = new Room("Room 1", "Room description 1", mockProject);
        Room room2 = new Room("Room 2", "Room description 2", mockProject);
        when(roomRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(room1, room2));

        // Act: Retrieve the rooms owned by the user
        Iterable<Room> userRooms = roomService.getUserRooms();

        // Assert: Verify that the result contains only the rooms owned by the user
        assertThat(userRooms).containsExactly(room1, room2);
    }

    @Test
    void getUserRooms_ShouldReturnRoomsAcrossMultipleProjects() {
        // Arrange: Set up two projects for the same user with rooms
        Project project1 = new Project("Project 1", mockUser);
        Project project2 = new Project("Project 2", mockUser);

        Room room1 = new Room("Room 1", "Description 1", project1);
        Room room2 = new Room("Room 2", "Description 2", project2);

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
    void updateRoom_ShouldUpdateRoom_WhenRoomExists() {
        // Arrange: Set up mock room with initial values and stub the repository to
        // return the room by ID
        Room room = new Room("Old Name", "Old Description", mockProject);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // Arrange: Create an UpdateRoomDTO with updated values
        UpdateRoomDTO roomDTO = new UpdateRoomDTO("Updated Name", "Updated Description");

        // Stub the repository to return the room after saving
        when(roomRepository.save(room)).thenReturn(room);

        // Act: Update the room using the service
        Room updatedRoom = roomService.updateRoom(1L, roomDTO);

        // Assert: Verify that the room's name was updated correctly
        assertThat(updatedRoom.getName()).isEqualTo("Updated Name");
        assertThat(updatedRoom.getDescription()).isEqualTo("Updated Description");
        verify(roomRepository).save(room);
    }

    @Test
    void updateRoom_ShouldThrowException_WhenRoomDoesNotExist() {
        // Arrange: Stub the repository to return an empty result when searching for a
        // non-existent room
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Arrange: Set up an UpdateRoomDTO with updated values
        UpdateRoomDTO roomDTO = new UpdateRoomDTO("Updated Name", "Updated Description");

        // Act & Assert: Attempt to update the room and expect a RoomNotFoundException
        assertThrows(RoomNotFoundException.class, () -> roomService.updateRoom(1L, roomDTO));
    }

    @Test
    void updateRoom_ShouldNotChangeDescription_WhenDescriptionIsNull() {
        // Arrange: Set up a room with an initial description
        Room room = new Room("Room Name", "Initial Description", mockProject);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // Stub the repository to return the room after saving
        when(roomRepository.save(room)).thenReturn(room);

        // Arrange: Set up an UpdateRoomDTO with null description
        UpdateRoomDTO roomDTO = new UpdateRoomDTO("Updated Name", null);

        // Act: Update room
        Room updatedRoom = roomService.updateRoom(1L, roomDTO);

        // Assert: Verify that the name was updated but the description remains the same
        assertThat(updatedRoom.getName()).isEqualTo("Updated Name");
        assertThat(updatedRoom.getDescription()).isEqualTo("Initial Description");
        verify(roomRepository).save(room);
    }

    @Test
    void addOrgUnitToRoom_Success() {
        // Arrange: Create a project, a room, and an orgUnit
        Project project = new Project("Test Project", mockUser);
        Room room = new Room("Test Room", "Room Description", project);
        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", project);

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(orgUnitRepository.findById(orgUnit.getId())).thenReturn(Optional.of(orgUnit));
        when(roomRepository.save(room)).thenReturn(room);

        // Act: Add the orgUnit to the room
        Room updatedRoom = roomService.addOrgUnitToRoom(room.getId(), orgUnit.getId());

        // Assert: Verify that the orgUnit is now associated with the room
        assertThat(updatedRoom.getOrgUnits()).contains(orgUnit);
        assertThat(orgUnit.getRoom()).isEqualTo(room);
    }

    @Test
    void addOrgUnitToRoom_DifferentProjects_ShouldThrowIllegalArgumentException() {
        // Arrange: Create two projects, each with its own room and orgUnit
        Project project1 = new Project("Project 1", mockUser);
        Project project2 = new Project("Project 2", mockUser);

        Room room = new Room("Test Room", "Room Description", project1);
        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", project2);

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(orgUnitRepository.findById(orgUnit.getId())).thenReturn(Optional.of(orgUnit));

        // Act & Assert: Attempt to add the orgUnit to a room in a different project
        assertThrows(IllegalArgumentException.class, () -> {
            roomService.addOrgUnitToRoom(room.getId(), orgUnit.getId());
        });
    }

    @Test
    void addOrgUnitToRoom_RoomNotFound_ShouldThrowRoomNotFoundException() {
        // Arrange: Ensure no room exists with the given ID
        Long nonExistentRoomId = 999L;
        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", mockProject);

        // Act & Assert: Attempt to add the orgUnit to a non-existent room
        assertThrows(RoomNotFoundException.class, () -> {
            roomService.addOrgUnitToRoom(nonExistentRoomId, orgUnit.getId());
        });
    }

    @Test
    void addOrgUnitToRoom_OrgUnitNotFound_ShouldThrowOrgUnitNotFoundException() {
        // Arrange: Create a room and ensure no orgUnit exists with the given ID
        Room room = new Room("Test Room", "Room Description", mockProject);
        Long nonExistentOrgUnitId = 999L;

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        // Act & Assert: Attempt to add a non-existent orgUnit to the room
        assertThrows(OrgUnitNotFoundException.class, () -> {
            roomService.addOrgUnitToRoom(room.getId(), nonExistentOrgUnitId);
        });
    }

    @Test
    void removeOrgUnitFromRoom_Success() {
        // Arrange: Create a room and add an orgUnit to it
        Project project = new Project("Test Project", mockUser);
        Room room = new Room("Test Room", "Room Description", project);
        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", room);
        room.addOrgUnit(orgUnit);

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(orgUnitRepository.findById(orgUnit.getId())).thenReturn(Optional.of(orgUnit));
        when(roomRepository.save(room)).thenReturn(room);

        // Act: Remove the orgUnit from the room
        Room updatedRoom = roomService.removeOrgUnitFromRoom(room.getId(), orgUnit.getId());

        // Assert: Verify that the orgUnit is no longer associated with the room
        assertThat(updatedRoom.getOrgUnits()).doesNotContain(orgUnit);
        assertThat(orgUnit.getRoom()).isNull();
    }

    @Test
    void removeOrgUnitFromRoom_RoomNotFound_ShouldThrowRoomNotFoundException() {
        // Arrange: Ensure no room exists with the given ID
        Long nonExistentRoomId = 999L;
        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", mockProject);

        // Act & Assert: Attempt to remove an orgUnit from a non-existent room
        assertThrows(RoomNotFoundException.class, () -> {
            roomService.removeOrgUnitFromRoom(nonExistentRoomId, orgUnit.getId());
        });
    }

    @Test
    void removeOrgUnitFromRoom_OrgUnitNotFound_ShouldThrowOrgUnitNotFoundException() {
        // Arrange: Create a room and ensure no orgUnit exists with the given ID
        Room room = new Room("Test Room", "Room Description", mockProject);
        Long nonExistentOrgUnitId = 999L;

        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        // Act & Assert: Attempt to remove a non-existent orgUnit from the room
        assertThrows(OrgUnitNotFoundException.class, () -> {
            roomService.removeOrgUnitFromRoom(room.getId(), nonExistentOrgUnitId);
        });
    }

    @Test
    void deleteRoom_ShouldDeleteRoom_WhenRoomExists() {
        // Arrange: Set up a room and stub the repository to return the room by ID
        Room room = new Room("Sample Room", "Room Description", mockProject);
        Long roomId = room.getId();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // Act: Delete the room using the service
        roomService.deleteRoom(roomId);

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
        assertThrows(RoomNotFoundException.class, () -> roomService.deleteRoom(1L));

        // Assert: Verify that the repository's delete method was never called
        verify(roomRepository, never()).deleteById(anyLong());
    }
}
