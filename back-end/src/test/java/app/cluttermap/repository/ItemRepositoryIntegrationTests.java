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
        User user1 = new User("owner1ProviderId");
        User user2 = new User("owner2ProviderId");
        userRepository.saveAll(List.of(user1, user2));

        Project project1 = new TestDataFactory.ProjectBuilder().user(user1).build();
        Project project2 = new TestDataFactory.ProjectBuilder().user(user2).build();
        projectRepository.saveAll(List.of(project1, project2));

        Room room1 = new TestDataFactory.RoomBuilder().project(project1).build();
        Room room2 = new TestDataFactory.RoomBuilder().project(project2).build();
        roomRepository.saveAll(List.of(room1, room2));

        OrgUnit orgUnit1 = new TestDataFactory.OrgUnitBuilder().room(room1).build();
        OrgUnit orgUnit2 = new TestDataFactory.OrgUnitBuilder().room(room2).build();
        orgUnitRepository.saveAll(List.of(orgUnit1, orgUnit2));

        Item item1 = new TestDataFactory.ItemBuilder().name("Item Owned by User 1").orgUnit(orgUnit1).build();
        Item item2 = new TestDataFactory.ItemBuilder().name("Item Owned by User 2").orgUnit(orgUnit2).build();

        itemRepository.saveAll(List.of(item1, item2));

        // Act: Retrieve items associated with user1
        List<Item> user1Items = itemRepository.findByOwnerId(user1.getId());

        // Assert: Verify that only the item owned by user1 is returned
        assertThat(user1Items).hasSize(1);
        assertThat(user1Items.get(0).getName()).isEqualTo("Item Owned by User 1");

        // Assert: Confirm that the item list does not contain a item owned by
        // user2
        assertThat(user1Items).doesNotContain(item2);
    }

    @Test
    void findByOwner_ShouldReturnAllItemsOwnedByUser() {
        // Arrange: Set up a user with multiple items
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new TestDataFactory.ProjectBuilder().user(owner).build();
        projectRepository.save(project);

        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        roomRepository.save(room);

        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().room(room).build();
        orgUnitRepository.save(orgUnit);

        List<String> itemNames = List.of("Item 1", "Item 2", "Item 3");
        Item item1 = new TestDataFactory.ItemBuilder().name(itemNames.get(0)).orgUnit(orgUnit).build();
        Item item2 = new TestDataFactory.ItemBuilder().name(itemNames.get(1)).orgUnit(orgUnit).build();
        Item item3 = new TestDataFactory.ItemBuilder().name(itemNames.get(2)).orgUnit(orgUnit).build();

        itemRepository.saveAll(List.of(item1, item2, item3));

        // Act: Retrieve all items associated with the user
        List<Item> ownerItems = itemRepository.findByOwnerId(owner.getId());

        // Assert: Verify that all items owned by the user are returned
        assertThat(ownerItems).hasSize(3);
        assertThat(ownerItems).extracting(Item::getName).containsExactlyInAnyOrder(itemNames.toArray(new String[0]));
    }

    @Test
    void findByOwner_ShouldReturnEmptyList_WhenUserHasNoItems() {
        // Arrange: Set up a user with no items
        User owner = new User("ownerProviderId");
        userRepository.save(owner); // Save the user without any items

        // Act: Retrieve items associated with the user
        List<Item> ownerItems = itemRepository.findByOwnerId(owner.getId());

        // Assert: Verify that the returned list is empty
        assertThat(ownerItems).isEmpty();
    }

    @Test
    void findUnassignedItemsByProjectId_ShouldReturnOnlyUnassignedItems() {
        // Arrange: Create a project and items with and without an assigned orgUnit
        User owner = new User("ownerProviderId");
        userRepository.save(owner);
        Project project = new TestDataFactory.ProjectBuilder().user(owner).build();
        projectRepository.save(project);

        List<String> itemNames = List.of("Item 1", "Item 2");
        Item unassignedItem1 = new TestDataFactory.ItemBuilder().name(itemNames.get(0)).project(project).build();
        Item unassignedItem2 = new TestDataFactory.ItemBuilder().name(itemNames.get(1)).project(project).build();

        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().project(project).build();
        orgUnitRepository.save(orgUnit);

        Item assignedItem = new TestDataFactory.ItemBuilder().orgUnit(orgUnit).build();
        assignedItem.setOrgUnit(orgUnit);

        itemRepository.saveAll(List.of(unassignedItem1, unassignedItem2, assignedItem));

        // Act: Retrieve unassigned items
        List<Item> unassignedItems = itemRepository.findUnassignedItemsByProjectId(project.getId());

        // Assert: Verify only unassigned items are returned
        assertThat(unassignedItems).extracting(Item::getName)
                .containsExactlyInAnyOrder(itemNames.toArray(new String[0]));
    }

    @Test
    void findUnassignedItemsByNonExistentProjectId_ShouldReturnEmptyList() {
        // Arrange: Use a project ID that does not exist in the database
        Long nonExistentProjectId = 999L;

        // Act: Retrieve unassigned items for the non-existent project
        List<Item> unassignedItems = itemRepository.findUnassignedItemsByProjectId(nonExistentProjectId);

        // Assert: Verify that the result is an empty list
        assertThat(unassignedItems).isEmpty();
    }
}