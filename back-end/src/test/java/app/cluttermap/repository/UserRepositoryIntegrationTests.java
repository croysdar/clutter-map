package app.cluttermap.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.EnableTestcontainers;
import app.cluttermap.TestDataFactory;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableTestcontainers
class UserRepositoryIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void user_ShouldHaveNonNullCreatedAt_WhenSaved() {
        // Arrange: Create a new User instance
        User user = new User("provider123");

        // Act: Save the user to the repository
        User savedUser = userRepository.save(user);

        // Assert: Verify that createdAt is set after saving
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    @Transactional
    void deletingUser_ShouldAlsoDeleteProjects() {
        // Arrange: Set up a user and create a project
        User user = new User("providerId");

        Project project1 = createProjectWithUserAndSave(user);
        Project project2 = createProjectWithUserAndSave(user);
        user.addProject(project1);
        user.addProject(project2);

        // Arrange: Save the user (and implicitly the project) to the repository
        userRepository.save(user);
        assertThat(projectRepository.findAll()).hasSize(2); // Verify that the projects saved

        // Act: Delete the user, triggering cascade deletion for the associated project
        userRepository.delete(user);

        // Assert: Verify that the project was deleted as an orphan when the room was
        // removed
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void removingProjectFromUser_ShouldTriggerOrphanRemoval() {
        // Arrange: Set up a user and create a project with an associated room
        User user = createUserAndSave();

        Project project1 = createProjectWithUserAndSave(user);
        Project project2 = createProjectWithUserAndSave(user);
        user.addProject(project1);
        user.addProject(project2);

        // Arrange: Save the user (and implicitly the project) to the repository
        userRepository.save(user);
        assertThat(projectRepository.findAll()).hasSize(2); // Verify that the projects saved

        // Clear the persistence context to ensure the latest state is fetched
        entityManager.clear();

        // Act: Remove the project from the user's project list and save the user to
        // trigger orphan removal
        user.removeProject(project1);
        userRepository.save(user);

        // Assert: Verify that only one project remains and the removed project was
        // deleted as an orphan
        assertThat(projectRepository.findAll()).containsExactly(project2);
    }

    private User createUserAndSave() {
        User owner = userRepository.save(new User("ownerProviderId"));
        return owner;
    }

    private Project createProjectWithUserAndSave(User owner) {
        Project project = projectRepository
                .save(new TestDataFactory.ProjectBuilder().id(null).user(owner).build());
        return project;
    }
}
