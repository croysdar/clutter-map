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
        User user1 = createUserAndSave();
        Project project1 = createProjectWithUserAndSave(user1);
        Room room1 = createRoomInProjectAndSave(project1);

        User user2 = createUserAndSave();
        Project project2 = createProjectWithUserAndSave(user2);
        Room room2 = createRoomInProjectAndSave(project2);

        // Act: Retrieve rooms associated with user1
        List<Room> user1Rooms = roomRepository.findByOwnerId(user1.getId());

        // Assert: Verify that only the room owned by user1 is returned
        assertThat(user1Rooms).containsExactly(room1);

        // Assert: Confirm that the room list does not contain a room owned by user2
        assertThat(user1Rooms).doesNotContain(room2);
    }

    @Test
    void findByOwner_ShouldReturnAllRoomsOwnedByUser() {
        // Arrange: Set up a user with multiple rooms
        User owner = createUserAndSave();
        Project project = createProjectWithUserAndSave(owner);

        Room room1 = createRoomInProjectAndSave(project);
        Room room2 = createRoomInProjectAndSave(project);
        Room room3 = createRoomInProjectAndSave(project);

        // Act: Retrieve all rooms associated with the user
        List<Room> ownerRooms = roomRepository.findByOwnerId(owner.getId());

        // Assert: Verify that all rooms owned by the user are returned
        assertThat(ownerRooms).containsExactlyInAnyOrder(room1, room2, room3);

    }

    @Test
    void findByOwner_ShouldReturnEmptyList_WhenUserHasNoRooms() {
        // Arrange: Set up a user with no rooms
        User owner = createUserAndSave();

        // Act: Retrieve rooms associated with the user
        List<Room> ownerRooms = roomRepository.findByOwnerId(owner.getId());

        // Assert: Verify that the returned list is empty
        assertThat(ownerRooms).isEmpty();
    }

    @Test
    @Transactional
    void deletingRoom_ShouldNotDeleteOrgUnitsButUnassignThem() {
        // Arrange: Set up a user and create a room with an associated orgUnit
        Room room = createRoomInProjectAndSave();
        OrgUnit orgUnit = createOrgUnitInRoomAndSave(room);

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
        Room room = createRoomInProjectAndSave();
        OrgUnit orgUnit = createOrgUnitInRoomAndSave(room);

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

    private User createUserAndSave() {
        User owner = userRepository.save(new User("ownerProviderId"));
        return owner;
    }

    private Project createProjectWithUserAndSave() {
        User owner = createUserAndSave();

        Project project = projectRepository
                .save(new TestDataFactory.ProjectBuilder().id(null).user(owner).build());
        return project;
    }

    private Project createProjectWithUserAndSave(User owner) {
        Project project = projectRepository
                .save(new TestDataFactory.ProjectBuilder().id(null).user(owner).build());
        return project;
    }

    private Room createRoomInProjectAndSave() {
        Project project = createProjectWithUserAndSave();
        Room room = roomRepository.save(new TestDataFactory.RoomBuilder().id(null).project(project).build());

        project.addRoom(room);
        projectRepository.save(project);
        return room;
    }

    private Room createRoomInProjectAndSave(Project project) {
        Room room = roomRepository.save(new TestDataFactory.RoomBuilder().id(null).project(project).build());

        project.addRoom(room);
        projectRepository.save(project);
        return room;
    }

    private OrgUnit createOrgUnitInRoomAndSave(Room room) {
        OrgUnit orgUnit = orgUnitRepository.save(new TestDataFactory.OrgUnitBuilder().id(null).room(room).build());

        room.addOrgUnit(orgUnit);
        roomRepository.save(room);
        return orgUnit;
    }
}
