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

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableTestcontainers
public class ItemRepositoryIntegrationTests {

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
    void findByOwner_ShouldReturnOnlyItemsOwnedBySpecifiedUser() {
        // Arrange: Set up two users, each with their own project, room and item
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

        Item item1 = new Item("Item Owned by Owner 1", "Item Description", List.of("tag 1"), orgUnit1);
        Item item2 = new Item("Item Owned by Owner 2", "Item Description", List.of("tag 2"), orgUnit2);
        itemRepository.saveAll(List.of(item1, item2));

        // Act: Retrieve items associated with owner1
        List<Item> owner1Items = itemRepository.findItemsByUserId(owner1.getId());

        // Assert: Verify that only the item owned by owner1 is returned
        assertThat(owner1Items).hasSize(1);
        assertThat(owner1Items.get(0).getName()).isEqualTo("Item Owned by Owner 1");

        // Assert: Confirm that the item list does not contain a item owned by
        // owner2
        assertThat(owner1Items).doesNotContain(item2);
    }

    @Test
    void findByOwner_ShouldReturnAllItemsOwnedByUser() {
        // Arrange: Set up a user with multiple items
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Project", owner);
        projectRepository.save(project);

        Room room = new Room("Room", "Room Description", project);
        roomRepository.save(room);

        OrgUnit orgUnit = new OrgUnit("OrgUnit", "OrgUnit Description", room);
        orgUnitRepository.save(orgUnit);

        Item item1 = new Item("Item 1", "Item Description", List.of("tag 1"), orgUnit);
        Item item2 = new Item("Item 2", "Item Description", List.of("tag 1"), orgUnit);
        Item item3 = new Item("Item 3", "Item Description", List.of("tag 1"), orgUnit);
        itemRepository.saveAll(List.of(item1, item2, item3));

        // Act: Retrieve all items associated with the user
        List<Item> ownerItems = itemRepository.findItemsByUserId(owner.getId());

        // Assert: Verify that all items owned by the user are returned
        assertThat(ownerItems).hasSize(3);
        assertThat(ownerItems).extracting(Item::getName).containsExactlyInAnyOrder("Item 1", "Item 2", "Item 3");
    }

    @Test
    void findByOwner_ShouldReturnEmptyList_WhenUserHasNoItems() {
        // Arrange: Set up a user with no items
        User owner = new User("ownerProviderId");
        userRepository.save(owner); // Save the user without any items

        // Act: Retrieve items associated with the user
        List<Item> ownerItems = itemRepository.findItemsByUserId(owner.getId());

        // Assert: Verify that the returned list is empty
        assertThat(ownerItems).isEmpty();
    }
}