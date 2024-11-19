package app.cluttermap.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.EnableTestcontainers;
import app.cluttermap.TestDataFactory;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableTestcontainers
public class RoomRepositoryIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private OrgUnitRepository orgUnitRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        projectRepository.deleteAll();
        roomRepository.deleteAll();
        orgUnitRepository.deleteAll();
    }

    @Test
    void findByOwner_ShouldReturnOnlyRoomsOwnedBySpecifiedUser() {
        // Arrange: Set up two users, each with their own project and room
        User user1 = new User("owner1ProviderId");
        User user2 = new User("owner2ProviderId");
        userRepository.saveAll(List.of(user1, user2));

        Project project1 = new Project("Project 1", user1);
        Project project2 = new Project("Project 2", user2);
        projectRepository.saveAll(List.of(project1, project2));

        Room room1 = new TestDataFactory.RoomBuilder().name("Room Owned by User 1").project(project1).build();
        Room room2 = new TestDataFactory.RoomBuilder().name("Room Owned by User 2").project(project2).build();
        roomRepository.saveAll(List.of(room1, room2));

        // Act: Retrieve rooms associated with user1
        List<Room> user1Rooms = roomRepository.findByOwnerId(user1.getId());

        // Assert: Verify that only the room owned by user1 is returned
        assertThat(user1Rooms).hasSize(1);
        assertThat(user1Rooms.get(0).getName()).isEqualTo("Room Owned by User 1");

        // Assert: Confirm that the room list does not contain a room owned by user2
        assertThat(user1Rooms).doesNotContain(room2);
    }

    @Test
    void findByOwner_ShouldReturnAllRoomsOwnedByUser() {
        // Arrange: Set up a user with multiple rooms
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Project", owner);
        projectRepository.save(project);

        List<String> roomNames = List.of("OrgUnit 1", "OrgUnit 2", "OrgUnit 3");
        Room room1 = new TestDataFactory.RoomBuilder().name(roomNames.get(0)).project(project).build();
        Room room2 = new TestDataFactory.RoomBuilder().name(roomNames.get(1)).project(project).build();
        Room room3 = new TestDataFactory.RoomBuilder().name(roomNames.get(2)).project(project).build();

        roomRepository.saveAll(List.of(room1, room2, room3));

        // Act: Retrieve all rooms associated with the user
        List<Room> ownerRooms = roomRepository.findByOwnerId(owner.getId());

        // Assert: Verify that all rooms owned by the user are returned
        assertThat(ownerRooms).hasSize(3);
        assertThat(ownerRooms).extracting(Room::getName)
                .containsExactlyInAnyOrder(roomNames.toArray(new String[0]));

    }

    @Test
    void findByOwner_ShouldReturnEmptyList_WhenUserHasNoRooms() {
        // Arrange: Set up a user with no rooms
        User owner = new User("ownerProviderId");
        userRepository.save(owner); // Save the user without any rooms

        // Act: Retrieve rooms associated with the user
        List<Room> ownerRooms = roomRepository.findByOwnerId(owner.getId());

        // Assert: Verify that the returned list is empty
        assertThat(ownerRooms).isEmpty();
    }

    @Test
    @Transactional
    void deletingRoom_ShouldNotDeleteOrgUnitsButUnassignThem() {
        // Arrange: Set up a user and create a room with an associated orgUnit
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Project", owner);
        projectRepository.save(project);

        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().room(room).build();
        room.getOrgUnits().add(orgUnit);
        roomRepository.save(room);

        assertThat(orgUnitRepository.findAll()).hasSize(1);

        // Act: Delete the room, triggering cascade deletion for the associated orgUnit
        roomRepository.delete(room);

        // Assert: Verify that the orgUnit still exists and is now "unassigned" (i.e.,
        // its room is null)
        OrgUnit fetchedOrgUnit = orgUnitRepository.findById(orgUnit.getId()).orElse(null);
        assertThat(fetchedOrgUnit).isNotNull();
        assertThat(fetchedOrgUnit.getRoom()).isNull();
    }

    @Test
    @Transactional
    void removingOrgUnitFromRoom_ShouldNotTriggerOrphanRemoval() {
        // Arrange: Set up a user and create a room with an associated orgUnit
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Project", owner);
        projectRepository.save(project);

        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().room(room).build();
        room.getOrgUnits().add(orgUnit);
        roomRepository.save(room);

        assertThat(orgUnitRepository.findAll()).hasSize(1);

        // Act: Remove the orgUnit from the room's orgUnit list and save the room
        // room.getOrgUnits().remove(orgUnit);
        room.removeOrgUnit(orgUnit);
        roomRepository.save(room);

        // Assert: Verify that the orgUnit was not deleted
        OrgUnit fetchedOrgUnit = orgUnitRepository.findById(orgUnit.getId()).orElse(null);
        assertThat(fetchedOrgUnit).isNotNull();
        assertThat(fetchedOrgUnit.getRoom()).isNull();
    }
}
