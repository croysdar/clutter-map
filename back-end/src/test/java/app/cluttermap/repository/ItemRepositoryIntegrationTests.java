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
    private OrgUnitRepository orgUnitRepository;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        projectRepository.deleteAll();
        orgUnitRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void findByOwner_ShouldReturnOnlyItemsOwnedBySpecifiedUser() {
        // Arrange: Set up two users, each with their own project and item
        User user1 = createUserAndSave();
        Project project1 = createProjectWithUserAndSave(user1);
        Item item1 = createItemInProjectAndSave(project1);

        User user2 = createUserAndSave();
        Project project2 = createProjectWithUserAndSave(user2);
        Item item2 = createItemInProjectAndSave(project2);

        // Act: Retrieve items associated with user1
        List<Item> user1Items = itemRepository.findByOwnerId(user1.getId());

        // Assert: Verify that only the item owned by user1 is returned
        assertThat(user1Items).containsExactly(item1);

        // Assert: Confirm that the item list does not contain a item owned by
        // user2
        assertThat(user1Items).doesNotContain(item2);
    }

    @Test
    void findByOwner_ShouldReturnAllItemsOwnedByUser() {
        // Arrange: Set up a user with multiple items, both assigned and unassigned
        User owner = createUserAndSave();

        Project project = createProjectWithUserAndSave(owner);
        Item item1 = createItemInProjectAndSave(project);
        Item item2 = createItemInProjectAndSave(project);

        OrgUnit orgUnit = createOrgUnitInProjectAndSave(project);
        Item item3 = createItemInOrgUnitAndSave(orgUnit);

        // Act: Retrieve all items associated with the user
        List<Item> ownerItems = itemRepository.findByOwnerId(owner.getId());

        // Assert: Verify that all items owned by the user are returned
        assertThat(ownerItems).containsExactlyInAnyOrder(item1, item2, item3);
    }

    @Test
    void findByOwner_ShouldReturnEmptyList_WhenUserHasNoItems() {
        // Arrange: Set up a user with no items
        User owner = createUserAndSave();

        // Act: Retrieve items associated with the user
        List<Item> ownerItems = itemRepository.findByOwnerId(owner.getId());

        // Assert: Verify that the returned list is empty
        assertThat(ownerItems).isEmpty();
    }

    @Test
    void findUnassignedItemsByProjectId_ShouldReturnOnlyUnassignedItems() {
        // Arrange: Create a project and items with and without an assigned orgUnit
        Project project = createProjectWithUserAndSave();
        Item unassignedItem1 = createItemInProjectAndSave(project);
        Item unassignedItem2 = createItemInProjectAndSave(project);

        // Save an assigned item as well
        OrgUnit orgUnit = createOrgUnitInProjectAndSave(project);
        createItemInOrgUnitAndSave(orgUnit);

        // Act: Retrieve unassigned items
        List<Item> unassignedItems = itemRepository.findUnassignedItemsByProjectId(project.getId());

        // Assert: Verify only unassigned items are returned
        assertThat(unassignedItems).containsExactlyInAnyOrder(unassignedItem1, unassignedItem2);
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

    private OrgUnit createOrgUnitInProjectAndSave(Project project) {
        OrgUnit orgUnit = orgUnitRepository
                .save(new TestDataFactory.OrgUnitBuilder().id(null).project(project).build());

        project.addOrgUnit(orgUnit);
        projectRepository.save(project);
        return orgUnit;
    }

    private Item createItemInProjectAndSave(Project project) {
        Item item = itemRepository.save(new TestDataFactory.ItemBuilder().id(null).project(project).build());

        project.addItem(item);
        projectRepository.save(project);
        return item;
    }

    private Item createItemInOrgUnitAndSave(OrgUnit orgUnit) {
        Item item = itemRepository.save(new TestDataFactory.ItemBuilder().id(null).orgUnit(orgUnit).build());

        orgUnit.addItem(item);
        orgUnitRepository.save(orgUnit);
        return item;
    }
}