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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableTestcontainers
public class ProjectRepositoryIntegrationTests {
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

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        projectRepository.deleteAll();
        roomRepository.deleteAll();
        orgUnitRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void findByOwner_ShouldReturnOnlyProjectsOwnedBySpecifiedUser() {
        // Arrange: Set up two users, each with their own project
        User user1 = createUserAndSave();
        Project project1 = createProjectWithUserAndSave(user1);

        User user2 = createUserAndSave();
        Project project2 = createProjectWithUserAndSave(user2);

        // Act: Retrieve projects associated with user1
        List<Project> user1Projects = projectRepository.findByOwnerId(user1.getId());

        // Assert: Verify that only the project owned by user1 is returned
        assertThat(user1Projects).containsExactly(project1);

        // Assert: Confirm that the project list does not contain a project owned by
        // user2
        assertThat(user1Projects).doesNotContain(project2);
    }

    @Test
    void findByOwner_ShouldReturnAllProjectsOwnedByUser() {
        // Arrange: Set up a user with multiple projects
        User owner = createUserAndSave();
        Project project1 = createProjectWithUserAndSave(owner);
        Project project2 = createProjectWithUserAndSave(owner);
        Project project3 = createProjectWithUserAndSave(owner);

        // Act: Retrieve all projects associated with the user
        List<Project> ownerProjects = projectRepository.findByOwnerId(owner.getId());

        // Assert: Verify that all projects owned by the user are returned
        assertThat(ownerProjects).containsExactlyInAnyOrder(project1, project2, project3);
    }

    @Test
    void findByOwner_ShouldReturnEmptyList_WhenUserHasNoProjects() {
        // Arrange: Set up a user with no projects
        User owner = createUserAndSave();

        // Act: Retrieve projects associated with the user
        List<Project> ownerProjects = projectRepository.findByOwnerId(owner.getId());

        // Assert: Verify that the returned list is empty
        assertThat(ownerProjects).isEmpty();
    }

    @Test
    @Transactional
    void deletingProject_ShouldAlsoDeleteRooms() {
        // Arrange: Set up a project and add a room to it
        Project project = createProjectWithUserAndSave();
        createRoomInProjectAndSave(project);

        // Act: Delete the project, triggering cascade deletion for the associated room
        projectRepository.delete(project);

        // Assert: Verify that the room was deleted as an orphan when the project was
        // removed
        assertThat(roomRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void removingRoomFromProject_ShouldTriggerOrphanRemoval() {
        // Arrange: Set up a project and add a room to it
        Project project = createProjectWithUserAndSave();
        Room room = createRoomInProjectAndSave(project);

        // Clear the persistence context to ensure the latest state is fetched
        entityManager.clear();

        // Act: Remove the room from the project's room list and save the project to
        // trigger orphan removal
        project.removeRoom(room);
        projectRepository.save(project);

        // Assert: Verify that the room was deleted as an orphan when removed from the
        // project
        assertThat(project.getRooms()).isEmpty();
        assertThat(roomRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void deletingProject_ShouldAlsoDeleteOrgUnits() {
        // Arrange: Set up a project and add an org unit to it
        Project project = createProjectWithUserAndSave();
        createOrgUnitInProjectAndSave(project);

        // Act: Delete the project, triggering cascade deletion for the associated
        // orgUnit
        projectRepository.delete(project);

        // Assert: Verify that the orgUnit was deleted as an orphan when the project was
        // removed
        assertThat(orgUnitRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void removingOrgUnitFromProject_ShouldTriggerOrphanRemoval() {
        // Arrange: Set up a project and add an org unit to it
        Project project = createProjectWithUserAndSave();
        OrgUnit orgUnit = createOrgUnitInProjectAndSave(project);

        // Clear the persistence context to ensure the latest state is fetched
        entityManager.clear();

        // Act: Remove the orgUnit from the project's orgUnit list and save the project
        // to trigger orphan removal
        project.removeOrgUnit(orgUnit);
        project = projectRepository.save(project);

        // Assert: Verify that the orgUnit was deleted as an orphan when removed from
        // the project
        assertThat(project.getOrgUnits()).isEmpty();
        assertThat(orgUnitRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void deletingProject_ShouldAlsoDeleteItems() {
        // Arrange: Set up a project and add an item to it
        Project project = createProjectWithUserAndSave();
        createItemInProjectAndSave(project);

        // Act: Delete the project, triggering cascade deletion for the associated item
        projectRepository.delete(project);

        // Assert: Verify that the item was deleted as an orphan when the project was
        // removed
        assertThat(itemRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void removingItemFromProject_ShouldTriggerOrphanRemoval() {
        // Arrange: Set up a project and add an item to it
        Project project = createProjectWithUserAndSave();
        Item item = createItemInProjectAndSave(project);

        // Clear the persistence context to ensure the latest state is fetched
        entityManager.clear();

        // Act: Remove the item from the project's item list and save the project to
        // trigger orphan removal
        project.removeItem(item);
        project = projectRepository.save(project);

        // Assert: Verify that the item was deleted as an orphan when removed from the
        // project
        assertThat(project.getItems()).isEmpty();
        assertThat(itemRepository.findAll()).isEmpty();
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

    private Item createItemInProjectAndSave(Project project) {
        Item item = itemRepository.save(new TestDataFactory.ItemBuilder().id(null).project(project).build());
        return item;
    }

}
