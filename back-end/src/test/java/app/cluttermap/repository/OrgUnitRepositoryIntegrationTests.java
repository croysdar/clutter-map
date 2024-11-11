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
    private UsersRepository userRepository;

    @Autowired
    private ProjectsRepository projectRepository;

    @Autowired
    private RoomsRepository roomRepository;

    @Autowired
    private OrgUnitsRepository orgUnitRepository;

    @Autowired
    private ItemsRepository itemRepository;

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
        User owner1 = new User("owner1ProviderId");
        User owner2 = new User("owner2ProviderId");
        userRepository.saveAll(List.of(owner1, owner2));

        Project project1 = new Project("Project 1", owner1);
        Project project2 = new Project("Project 2", owner2);
        projectRepository.saveAll(List.of(project1, project2));

        Room room1 = new Room("Room 1", "Room Description 1", project1);
        Room room2 = new Room("Room 2", "Room Description 2", project2);
        roomRepository.saveAll(List.of(room1, room2));

        OrgUnit orgUnit1 = new OrgUnit("OrgUnit Owned by Owner 1", "OrgUnit Description", room1);
        OrgUnit orgUnit2 = new OrgUnit("OrgUnit Owned by Owner 2", "OrgUnit Description", room2);
        orgUnitRepository.saveAll(List.of(orgUnit1, orgUnit2));

        // Act: Retrieve orgUnits associated with owner1
        List<OrgUnit> owner1OrgUnits = orgUnitRepository.findOrgUnitsByUserId(owner1.getId());

        // Assert: Verify that only the orgUnit owned by owner1 is returned
        assertThat(owner1OrgUnits).hasSize(1);
        assertThat(owner1OrgUnits.get(0).getName()).isEqualTo("OrgUnit Owned by Owner 1");

        // Assert: Confirm that the orgUnit list does not contain a orgUnit owned by
        // owner2
        assertThat(owner1OrgUnits).doesNotContain(orgUnit2);
    }

    @Test
    void findByOwner_ShouldReturnAllOrgUnitsOwnedByUser() {
        // Arrange: Set up a user with multiple orgUnits
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Project", owner);
        projectRepository.save(project);

        Room room = new Room("Room", "Room Description", project);
        roomRepository.save(room);

        OrgUnit orgUnit1 = new OrgUnit("OrgUnit 1", "OrgUnit Description", room);
        OrgUnit orgUnit2 = new OrgUnit("OrgUnit 2", "OrgUnit Description", room);
        OrgUnit orgUnit3 = new OrgUnit("OrgUnit 3", "OrgUnit Description", room);
        orgUnitRepository.saveAll(List.of(orgUnit1, orgUnit2, orgUnit3));

        // Act: Retrieve all orgUnits associated with the user
        List<OrgUnit> ownerOrgUnits = orgUnitRepository.findOrgUnitsByUserId(owner.getId());

        // Assert: Verify that all orgUnits owned by the user are returned
        assertThat(ownerOrgUnits).hasSize(3);
        assertThat(ownerOrgUnits).extracting(OrgUnit::getName).containsExactlyInAnyOrder("OrgUnit 1", "OrgUnit 2",
                "OrgUnit 3");
    }

    @Test
    void findByOwner_ShouldReturnEmptyList_WhenUserHasNoOrgUnits() {
        // Arrange: Set up a user with no orgUnits
        User owner = new User("ownerProviderId");
        userRepository.save(owner); // Save the user without any orgUnits

        // Act: Retrieve orgUnits associated with the user
        List<OrgUnit> ownerOrgUnits = orgUnitRepository.findOrgUnitsByUserId(owner.getId());

        // Assert: Verify that the returned list is empty
        assertThat(ownerOrgUnits).isEmpty();
    }

    @Test
    @Transactional
    void deletingOrgUnit_ShouldAlsoDeleteItems() {
        // Arrange: Set up a user and create a orgUnit with an associated item
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Project", owner);
        projectRepository.save(project);

        Room room = new Room("Room", "Room Description", project);
        roomRepository.save(room);

        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", room);
        Item item = new Item("Item", "Item Description", List.of("tag1"), orgUnit);
        orgUnit.getItems().add(item);
        orgUnitRepository.save(orgUnit);

        assertThat(itemRepository.findAll()).hasSize(1);

        // Act: Delete the orgUnit, triggering cascade deletion for the associated item
        orgUnitRepository.delete(orgUnit);

        // Assert: Verify that the item was deleted as an orphan when the orgUnit was
        // removed
        assertThat(itemRepository.findAll()).isEmpty();
    }

    /*
     * TODO make this be for project instead
     * 
     * @Test
     * 
     * @Transactional
     * void removingItemFromOrgUnit_ShouldTriggerOrphanRemoval() {
     * // Arrange: Set up a user and create a orgUnit with an associated item
     * User owner = new User("ownerProviderId");
     * userRepository.save(owner);
     * 
     * Project project = new Project("Project", owner);
     * projectRepository.save(project);
     * 
     * Room room = new Room("Room", "Room Description", project);
     * roomRepository.save(room);
     * 
     * OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", room);
     * Item item = new Item("Item", "Item Description", List.of("tag1"), orgUnit);
     * orgUnit.getItems().add(item);
     * orgUnitRepository.save(orgUnit);
     * 
     * assertThat(itemRepository.findAll()).hasSize(1);
     * 
     * // Act: Remove the item from the orgUnit's item list and save the orgUnit to
     * // trigger orphan removal
     * orgUnit.getItems().remove(item);
     * orgUnitRepository.save(orgUnit);
     * 
     * // Assert: Verify that the item was deleted as an orphan when removed from
     * // the orgUnit
     * assertThat(itemRepository.findAll()).isEmpty();
     * }
     * 
     */
}
