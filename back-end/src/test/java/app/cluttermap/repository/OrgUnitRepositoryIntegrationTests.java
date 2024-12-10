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
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableTestcontainers
public class OrgUnitRepositoryIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private OrgUnitRepository orgUnitRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        projectRepository.deleteAll();
        roomRepository.deleteAll();
        orgUnitRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void findByOwner_ShouldReturnOnlyOrgUnitsOwnedBySpecifiedUser() {
        // Arrange: Set up two users, each with their own project and orgUnit
        User user1 = createUserAndSave();
        Project project1 = createProjectWithUserAndSave(user1);
        OrgUnit orgUnit1 = createOrgUnitInProjectAndSave(project1);

        User user2 = createUserAndSave();
        Project project2 = createProjectWithUserAndSave(user2);
        OrgUnit orgUnit2 = createOrgUnitInProjectAndSave(project2);

        // Act: Retrieve orgUnits associated with owner1
        List<OrgUnit> user1OrgUnits = orgUnitRepository.findByOwnerId(user1.getId());

        // Assert: Verify that only the orgUnit owned by owner1 is returned
        assertThat(user1OrgUnits).containsExactly(orgUnit1);

        // Assert: Confirm that the orgUnit list does not contain a orgUnit owned by
        // owner2
        assertThat(user1OrgUnits).doesNotContain(orgUnit2);
    }

    @Test
    void findByOwner_ShouldReturnAllOrgUnitsOwnedByUser() {
        // Arrange: Set up a user with multiple orgUnits, both assigned and unassigned
        User owner = createUserAndSave();

        Project project = createProjectWithUserAndSave(owner);
        OrgUnit orgUnit1 = createOrgUnitInProjectAndSave(project);
        OrgUnit orgUnit2 = createOrgUnitInProjectAndSave(project);

        Room room = createRoomInProjectAndSave(project);
        OrgUnit orgUnit3 = createOrgUnitInRoomAndSave(room);

        // Act: Retrieve all orgUnits associated with the user
        List<OrgUnit> ownerOrgUnits = orgUnitRepository.findByOwnerId(owner.getId());

        // Assert: Verify that all orgUnits owned by the user are returned
        assertThat(ownerOrgUnits).containsExactlyInAnyOrder(orgUnit1, orgUnit2, orgUnit3);
    }

    @Test
    void findByOwner_ShouldReturnEmptyList_WhenUserHasNoOrgUnits() {
        // Arrange: Set up a user with no orgUnits
        User owner = createUserAndSave();

        // Act: Retrieve orgUnits associated with the user
        List<OrgUnit> ownerOrgUnits = orgUnitRepository.findByOwnerId(owner.getId());

        // Assert: Verify that the returned list is empty
        assertThat(ownerOrgUnits).isEmpty();
    }

    @Test
    @Transactional
    void deletingOrgUnit_ShouldNotDeleteItemsButUnassignThem() {
        // Arrange: Set an org unit with an item
        OrgUnit orgUnit = createOrgUnitInProjectAndSave();
        Item item = createItemInOrgUnitAndSave(orgUnit);

        assertThat(itemRepository.findAll()).hasSize(1);

        // Act: Delete the orgUnit
        orgUnitRepository.delete(orgUnit);

        // Assert: Verify that the item still exists and is now "unassigned" (i.e., its
        // orgUnit is null)
        Item fetchedItem = itemRepository.findById(item.getId()).orElse(null);
        assertThat(fetchedItem).isNotNull();
        assertThat(fetchedItem.getOrgUnit()).isNull();
    }

    @Test
    @Transactional
    void removingItemFromOrgUnit_ShouldNotTriggerOrphanRemoval() {
        // Arrange: Set up an org unit with an item
        OrgUnit orgUnit = createOrgUnitInProjectAndSave();
        Item item = createItemInOrgUnitAndSave(orgUnit);
        orgUnit.addItem(item); // Use addItem to set bidirectional relationship
        orgUnit = orgUnitRepository.save(orgUnit);

        assertThat(itemRepository.findAll()).hasSize(1);

        // Act: Remove the item from the orgUnit's item list and save the orgUnit
        orgUnit.removeItem(item);
        orgUnitRepository.save(orgUnit);

        // Assert: Verify that the item was not deleted but is unassigned (i.e., its
        // orgUnit is null)
        Item fetchedItem = itemRepository.findById(item.getId()).orElse(null);
        assertThat(fetchedItem).isNotNull();
        assertThat(fetchedItem.getOrgUnit()).isNull();
    }

    @Test
    void findUnassignedOrgUnitsByProjectId_ShouldReturnOnlyUnassignedOrgUnits() {
        // Arrange: Create a project and orgUnits with and without an assigned room
        Project project = createProjectWithUserAndSave();

        OrgUnit unassignedOrgUnit1 = createOrgUnitInProjectAndSave(project);
        OrgUnit unassignedOrgUnit2 = createOrgUnitInProjectAndSave(project);

        Room room = createRoomInProjectAndSave(project);

        // Create an assigned org unit as well
        OrgUnit assignedOrgUnit = createOrgUnitInRoomAndSave(room);

        // Act: Retrieve unassigned orgUnits
        List<OrgUnit> unassignedOrgUnits = orgUnitRepository.findUnassignedOrgUnitsByProjectId(project.getId());

        // Assert: Verify only unassigned orgUnits are returned
        assertThat(unassignedOrgUnits).containsExactlyInAnyOrder(unassignedOrgUnit1, unassignedOrgUnit2);
        assertThat(unassignedOrgUnits).doesNotContain(assignedOrgUnit);
    }

    @Test
    void findUnassignedOrgUnitsByNonExistentProjectId_ShouldReturnEmptyList() {
        // Arrange: Use a project ID that does not exist in the database
        Long nonExistentProjectId = 999L;

        // Act: Retrieve unassigned orgUnits for the non-existent project
        List<OrgUnit> unassignedOrgUnits = orgUnitRepository.findUnassignedOrgUnitsByProjectId(nonExistentProjectId);

        // Assert: Verify that the result is an empty list
        assertThat(unassignedOrgUnits).isEmpty();
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

    private Room createRoomInProjectAndSave(Project project) {
        Room room = roomRepository.save(new TestDataFactory.RoomBuilder().id(null).project(project).build());
        return room;
    }

    private OrgUnit createOrgUnitInProjectAndSave(Project project) {
        OrgUnit orgUnit = orgUnitRepository
                .save(new TestDataFactory.OrgUnitBuilder().id(null).project(project).build());
        return orgUnit;
    }

    private OrgUnit createOrgUnitInProjectAndSave() {
        Project project = createProjectWithUserAndSave();
        OrgUnit orgUnit = orgUnitRepository
                .save(new TestDataFactory.OrgUnitBuilder().id(null).project(project).build());
        return orgUnit;
    }

    private OrgUnit createOrgUnitInRoomAndSave(Room room) {
        OrgUnit orgUnit = orgUnitRepository
                .save(new TestDataFactory.OrgUnitBuilder().id(null).room(room).build());
        room.addOrgUnit(orgUnit);
        room = roomRepository.save(room);
        return orgUnit;
    }

    private Item createItemInOrgUnitAndSave(OrgUnit orgUnit) {
        Item item = itemRepository.save(new TestDataFactory.ItemBuilder().id(null).orgUnit(orgUnit).build());

        orgUnit.addItem(item); // Use addItem to set bidirectional relationship
        orgUnit = orgUnitRepository.save(orgUnit);
        return item;
    }
}
