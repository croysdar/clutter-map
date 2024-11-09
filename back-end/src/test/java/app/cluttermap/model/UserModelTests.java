package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class UserModelTests {
    @Test
    void user_ShouldInitializeFieldsCorrectly_WhenConstructedWithProviderId() {
        // Arrange & Act: Create a new user with only provider ID
        User user = new User("provider123");

        // Assert: Verify that fields are set correctly
        assertThat(user.getProviderId()).isEqualTo("provider123");
        assertThat(user.getProjects()).isEmpty(); // Projects should initialize as empty
    }

    @Test
    void user_ShouldSetAndGetFieldsCorrectly() {
        // Arrange: Create a user and set fields
        User user = new User("provider123");
        user.setUsername("testUser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setProvider("google");

        // Assert: Verify that each field is set correctly
        assertThat(user.getUsername()).isEqualTo("testUser");
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getProvider()).isEqualTo("google");
    }

    @Test
    void user_ShouldManageProjectsCorrectly() {
        // Arrange: Set up a user and create multiple projects
        User user = new User("ownerProviderId");
        Project project1 = new Project("Project 1", user);
        Project project2 = new Project("Project 2", user);
        List<Project> projects = new ArrayList<>(List.of(project1, project2));

        // Act: Set the list of projects on the user
        user.setProjects(projects);

        // Assert: Verify that the projects list is correctly assigned and linked to the
        // user
        assertThat(user.getProjects()).hasSize(2);
        assertThat(user.getProjects()).containsExactlyInAnyOrder(project1, project2);
        assertThat(user.getProjects().get(0).getOwner()).isEqualTo(user);
        assertThat(user.getProjects().get(1).getOwner()).isEqualTo(user);

        // Act: Remove a project by setting a new list
        user.setProjects(new ArrayList<>(List.of(project1)));

        // Assert: Verify that only the remaining project is present
        assertThat(user.getProjects()).hasSize(1);
        assertThat(user.getProjects().get(0).getName()).isEqualTo("Project 1");
    }
}
