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

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        projectRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    void findByOwner_ShouldReturnOnlyProjectsOwnedBySpecifiedUser() {
        // Arrange: Set up two users, each with their own project
        User owner1 = new User("owner1ProviderId");
        Project project1 = new Project("Project Owned by Owner 1", owner1);

        User owner2 = new User("owner2ProviderId");
        Project project2 = new Project("Project Owned by Owner 2", owner2);

        // Arrange: Save the users and their projects to the repositories
        userRepository.saveAll(List.of(owner1, owner2));
        projectRepository.saveAll(List.of(project1, project2));

        // Act: Retrieve projects associated with owner1
        List<Project> owner1Projects = projectRepository.findByOwnerId(owner1.getId());

        // Assert: Verify that only the project owned by owner1 is returned
        assertThat(owner1Projects).hasSize(1);
        assertThat(owner1Projects.get(0).getName()).isEqualTo("Project Owned by Owner 1");

        // Assert: Confirm that the project list does not contain a project owned by
        // owner2
        assertThat(owner1Projects).doesNotContain(project2);
    }

    @Test
    void findByOwner_ShouldReturnAllProjectsOwnedByUser() {
        // Arrange: Set up a user with multiple projects
        User owner = new User("ownerProviderId");
        Project project1 = new Project("Project 1", owner);
        Project project2 = new Project("Project 2", owner);
        Project project3 = new Project("Project 3", owner);

        // Arrange: Save the user and their projects to the repositories
        userRepository.save(owner);
        projectRepository.saveAll(List.of(project1, project2, project3));

        // Act: Retrieve all projects associated with the user
        List<Project> ownerProjects = projectRepository.findByOwnerId(owner.getId());

        // Assert: Verify that all projects owned by the user are returned
        assertThat(ownerProjects).hasSize(3);
        assertThat(ownerProjects).extracting(Project::getName).containsExactlyInAnyOrder("Project 1", "Project 2",
                "Project 3");
    }

    @Test
    void findByOwner_ShouldReturnEmptyList_WhenUserHasNoProjects() {
        // Arrange: Set up a user with no projects
        User owner = new User("ownerProviderId");
        userRepository.save(owner); // Save the user without any projects

        // Act: Retrieve projects associated with the user
        List<Project> ownerProjects = projectRepository.findByOwnerId(owner.getId());

        // Assert: Verify that the returned list is empty
        assertThat(ownerProjects).isEmpty();
    }

    @Test
    @Transactional
    void deletingProject_ShouldAlsoDeleteRooms() {
        // Arrange: Set up a user and create a project with an associated room
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Test Project", owner);
        Room room = new Room("Living Room", "This is the living room", project);
        project.getRooms().add(room);

        // Arrange: Save the project (and implicitly the room) to the repository
        projectRepository.save(project);
        assertThat(roomRepository.findAll()).hasSize(1); // Verify that the room saved

        // Act: Delete the project, triggering cascade deletion for the associated room
        projectRepository.delete(project);

        // Assert: Verify that the room was deleted as an orphan when the project was
        // removed
        assertThat(roomRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void removingRoomFromProject_ShouldTriggerOrphanRemoval() {
        // Arrange: Set up a user and create a project with an associated room
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Test Project", owner);
        Room room = new Room("Living Room", "This is the living room", project);
        project.getRooms().add(room);

        // Arrange: Save the project (and implicitly the room) to the repository
        projectRepository.save(project);
        assertThat(roomRepository.findAll()).hasSize(1); // Room should exist in DB

        // Act: Remove the room from the project's room list and save the project to
        // trigger orphan removal
        project.getRooms().remove(room);
        projectRepository.save(project);

        // Assert: Verify that the room was deleted as an orphan when removed from the
        // project
        assertThat(roomRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void deletingProject_ShouldAlsoDeleteOrgUnits() {
        // Arrange: Set up a user and create a project with an associated orgUnit
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Test Project", owner);
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().project(project).build();
        project.getOrgUnits().add(orgUnit);

        // Arrange: Save the project (and implicitly the orgUnit) to the repository
        projectRepository.save(project);
        assertThat(orgUnitRepository.findAll()).hasSize(1); // Verify that the orgUnit was saved

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
        // Arrange: Set up a user and create a project with an associated orgUnit
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Test Project", owner);
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().project(project).build();
        project.getOrgUnits().add(orgUnit);

        // Arrange: Save the project (and implicitly the orgUnit) to the repository
        projectRepository.save(project);
        assertThat(orgUnitRepository.findAll()).hasSize(1); // Verify that the orgUnit exists in DB

        // Act: Remove the orgUnit from the project's orgUnit list and save the project
        // to trigger orphan removal
        project.getOrgUnits().remove(orgUnit);
        projectRepository.save(project);

        // Assert: Verify that the orgUnit was deleted as an orphan when removed from
        // the project
        assertThat(orgUnitRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void deletingProject_ShouldAlsoDeleteItems() {
        // Arrange: Set up a user and create a project with an associated item
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Test Project", owner);
        Item item = new TestDataFactory.ItemBuilder().project(project).build();
        project.getItems().add(item);

        // Arrange: Save the project (and implicitly the item) to the repository
        projectRepository.save(project);
        assertThat(itemRepository.findAll()).hasSize(1); // Verify that the item was saved

        // Act: Delete the project, triggering cascade deletion for the associated item
        projectRepository.delete(project);

        // Assert: Verify that the item was deleted as an orphan when the project was
        // removed
        assertThat(itemRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void removingItemFromProject_ShouldTriggerOrphanRemoval() {
        // Arrange: Set up a user and create a project with an associated item
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Test Project", owner);
        Item item = new TestDataFactory.ItemBuilder().project(project).build();
        project.getItems().add(item);

        // Arrange: Save the project (and implicitly the item) to the repository
        projectRepository.save(project);
        assertThat(itemRepository.findAll()).hasSize(1); // Verify that the item exists in DB

        // Act: Remove the item from the project's item list and save the project to
        // trigger orphan removal
        project.getItems().remove(item);
        projectRepository.save(project);

        // Assert: Verify that the item was deleted as an orphan when removed from the
        // project
        assertThat(itemRepository.findAll()).isEmpty();
    }
}
