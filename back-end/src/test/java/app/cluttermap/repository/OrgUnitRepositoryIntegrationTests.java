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
        // Arrange: Set up two users, each with their own project, room and orgUnit
        User user1 = new User("owner1ProviderId");
        User user2 = new User("owner2ProviderId");
        userRepository.saveAll(List.of(user1, user2));

        Project project1 = new TestDataFactory.ProjectBuilder().user(user1).build();
        Project project2 = new TestDataFactory.ProjectBuilder().user(user2).build();
        projectRepository.saveAll(List.of(project1, project2));

        Room room1 = new TestDataFactory.RoomBuilder().project(project1).build();
        Room room2 = new TestDataFactory.RoomBuilder().project(project2).build();
        roomRepository.saveAll(List.of(room1, room2));

        OrgUnit orgUnit1 = new TestDataFactory.OrgUnitBuilder().name("OrgUnit Owned by User 1").room(room1).build();
        OrgUnit orgUnit2 = new TestDataFactory.OrgUnitBuilder().name("OrgUnit Owned by User 2").room(room2).build();

        orgUnitRepository.saveAll(List.of(orgUnit1, orgUnit2));

        // Act: Retrieve orgUnits associated with owner1
        List<OrgUnit> user1OrgUnits = orgUnitRepository.findByOwnerId(user1.getId());

        // Assert: Verify that only the orgUnit owned by owner1 is returned
        assertThat(user1OrgUnits).hasSize(1);
        assertThat(user1OrgUnits.get(0).getName()).isEqualTo("OrgUnit Owned by User 1");

        // Assert: Confirm that the orgUnit list does not contain a orgUnit owned by
        // owner2
        assertThat(user1OrgUnits).doesNotContain(orgUnit2);
    }

    @Test
    void findByOwner_ShouldReturnAllOrgUnitsOwnedByUser() {
        // Arrange: Set up a user with multiple orgUnits
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new TestDataFactory.ProjectBuilder().user(owner).build();
        projectRepository.save(project);

        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        roomRepository.save(room);

        List<String> orgUnitNames = List.of("OrgUnit 1", "OrgUnit 2", "OrgUnit 3");
        OrgUnit orgUnit1 = new TestDataFactory.OrgUnitBuilder().name(orgUnitNames.get(0)).room(room).build();
        OrgUnit orgUnit2 = new TestDataFactory.OrgUnitBuilder().name(orgUnitNames.get(1)).room(room).build();
        OrgUnit orgUnit3 = new TestDataFactory.OrgUnitBuilder().name(orgUnitNames.get(2)).room(room).build();
        orgUnitRepository.saveAll(List.of(orgUnit1, orgUnit2, orgUnit3));

        // Act: Retrieve all orgUnits associated with the user
        List<OrgUnit> ownerOrgUnits = orgUnitRepository.findByOwnerId(owner.getId());

        // Assert: Verify that all orgUnits owned by the user are returned
        assertThat(ownerOrgUnits).hasSize(3);
        assertThat(ownerOrgUnits).extracting(OrgUnit::getName)
                .containsExactlyInAnyOrder(orgUnitNames.toArray(new String[0]));
    }

    @Test
    void findByOwner_ShouldReturnEmptyList_WhenUserHasNoOrgUnits() {
        // Arrange: Set up a user with no orgUnits
        User owner = new User("ownerProviderId");
        userRepository.save(owner); // Save the user without any orgUnits

        // Act: Retrieve orgUnits associated with the user
        List<OrgUnit> ownerOrgUnits = orgUnitRepository.findByOwnerId(owner.getId());

        // Assert: Verify that the returned list is empty
        assertThat(ownerOrgUnits).isEmpty();
    }

    @Test
    @Transactional
    void deletingOrgUnit_ShouldNotDeleteItemsButUnassignThem() {
        // Arrange: Set up a user and create an orgUnit with an associated item
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new TestDataFactory.ProjectBuilder().user(owner).build();
        projectRepository.save(project);

        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        roomRepository.save(room);

        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().room(room).build();
        Item item = new TestDataFactory.ItemBuilder().orgUnit(orgUnit).build();
        orgUnit.addItem(item); // Use addItem to set bidirectional relationship
        orgUnitRepository.save(orgUnit);

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
        // Arrange: Set up a user and create an orgUnit with an associated item
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new TestDataFactory.ProjectBuilder().user(owner).build();
        projectRepository.save(project);

        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        roomRepository.save(room);

        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().room(room).build();
        Item item = new TestDataFactory.ItemBuilder().orgUnit(orgUnit).build();
        orgUnit.addItem(item); // Use addItem to set bidirectional relationship
        orgUnitRepository.save(orgUnit);

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
        User owner = new User("ownerProviderId");
        userRepository.save(owner);
        Project project = new TestDataFactory.ProjectBuilder().user(owner).build();
        projectRepository.save(project);

        OrgUnit unassignedOrgUnit1 = new TestDataFactory.OrgUnitBuilder().name("Unassigned OrgUnit 1").project(project)
                .build();
        OrgUnit unassignedOrgUnit2 = new TestDataFactory.OrgUnitBuilder().name("Unassigned OrgUnit 2").project(project)
                .build();

        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        roomRepository.save(room);
        OrgUnit assignedOrgUnit = new TestDataFactory.OrgUnitBuilder().name("Assigned OrgUnit").project(project)
                .build();
        assignedOrgUnit.setRoom(room);

        orgUnitRepository.saveAll(List.of(unassignedOrgUnit1, unassignedOrgUnit2, assignedOrgUnit));

        // Act: Retrieve unassigned orgUnits
        List<OrgUnit> unassignedOrgUnits = orgUnitRepository.findUnassignedOrgUnitsByProjectId(project.getId());

        // Assert: Verify only unassigned orgUnits are returned
        assertThat(unassignedOrgUnits).extracting(OrgUnit::getName)
                .containsExactlyInAnyOrder("Unassigned OrgUnit 1", "Unassigned OrgUnit 2");
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
}
