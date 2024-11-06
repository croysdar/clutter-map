package app.cluttermap.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import jakarta.transaction.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoomRepositoryIntegrationTests {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private ProjectsRepository projectRepository;

    @Autowired
    private RoomsRepository roomRepository;

    @Autowired
    private OrgUnitsRepository orgUnitRepository;

    @Test
    void findByOwner_ShouldReturnOnlyRoomsOwnedBySpecifiedUser() {
        // Arrange: Set up two users, each with their own project and room
        User owner1 = new User("owner1ProviderId");
        User owner2 = new User("owner2ProviderId");
        userRepository.saveAll(List.of(owner1, owner2));

        Project project1 = new Project("Project 1", owner1);
        Project project2 = new Project("Project 2", owner2);
        projectRepository.saveAll(List.of(project1, project2));

        Room room1 = new Room("Room Owned by Owner 1", "Room Description", project1);
        Room room2 = new Room("Room Owned by Owner 2", "Room Description", project2);
        roomRepository.saveAll(List.of(room1, room2));

        // Act: Retrieve rooms associated with owner1
        List<Room> owner1Rooms = roomRepository.findRoomsByProjectOwnerId(owner1.getId());

        // Assert: Verify that only the room owned by owner1 is returned
        assertThat(owner1Rooms).hasSize(1);
        assertThat(owner1Rooms.get(0).getName()).isEqualTo("Room Owned by Owner 1");

        // Assert: Confirm that the room list does not contain a room owned by owner2
        assertThat(owner1Rooms).doesNotContain(room2);
    }

    @Test
    void findByOwner_ShouldReturnAllRoomsOwnedByUser() {
        // Arrange: Set up a user with multiple rooms
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Project", owner);
        projectRepository.save(project);

        Room room1 = new Room("Room 1", "Room Description", project);
        Room room2 = new Room("Room 2", "Room Description", project);
        Room room3 = new Room("Room 3", "Room Description", project);
        roomRepository.saveAll(List.of(room1, room2, room3));

        // Act: Retrieve all rooms associated with the user
        List<Room> ownerRooms = roomRepository.findRoomsByProjectOwnerId(owner.getId());

        // Assert: Verify that all rooms owned by the user are returned
        assertThat(ownerRooms).hasSize(3);
        assertThat(ownerRooms).containsExactlyInAnyOrder(room1, room2, room3);
    }

    @Test
    void findByOwner_ShouldReturnEmptyList_WhenUserHasNoRooms() {
        // Arrange: Set up a user with no rooms
        User owner = new User("ownerProviderId");
        userRepository.save(owner); // Save the user without any rooms

        // Act: Retrieve rooms associated with the user
        List<Room> ownerRooms = roomRepository.findRoomsByProjectOwnerId(owner.getId());

        // Assert: Verify that the returned list is empty
        assertThat(ownerRooms).isEmpty();
    }

    @Test
    @Transactional
    void deletingRoom_ShouldAlsoDeleteOrgUnits() {
        // Arrange: Set up a user and create a room with an associated orgUnit
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Project", owner);
        projectRepository.save(project);

        Room room = new Room("Test Room", "Room Description", project);
        OrgUnit orgUnit = new OrgUnit("White Shelving Unit", "This is a shelving unit", room);
        room.getOrgUnits().add(orgUnit);
        roomRepository.save(room);

        assertThat(orgUnitRepository.findAll()).hasSize(1);

        // Act: Delete the room, triggering cascade deletion for the associated orgUnit
        roomRepository.delete(room);

        // Assert: Verify that the orgUnit was deleted as an orphan when the room was
        // removed
        assertThat(orgUnitRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void removingOrgUnitFromRoom_ShouldTriggerOrphanRemoval() {
        // Arrange: Set up a user and create a room with an associated orgUnit
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Project", owner);
        projectRepository.save(project);

        Room room = new Room("Test Room", "Room Description", project);
        OrgUnit orgUnit = new OrgUnit("White Shelving Unit", "This is a shelving unit", room);
        room.getOrgUnits().add(orgUnit);
        roomRepository.save(room);

        assertThat(orgUnitRepository.findAll()).hasSize(1);

        // Act: Remove the orgUnit from the room's orgUnit list and save the room to
        // trigger orphan removal
        room.getOrgUnits().remove(orgUnit);
        roomRepository.save(room);

        // Assert: Verify that the orgUnit was deleted as an orphan when removed from
        // the room
        assertThat(orgUnitRepository.findAll()).isEmpty();
    }
}
