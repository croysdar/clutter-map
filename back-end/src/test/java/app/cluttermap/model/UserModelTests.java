package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.TestDataFactory;

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

        Project project1 = new TestDataFactory.ProjectBuilder().name("Project 1").user(user).build();
        Project project2 = new TestDataFactory.ProjectBuilder().name("Project 2").user(user).build();
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

    @Test
    void toString_ShouldHandleNullFields() {
        User user = new User();
        user.setId(1L);

        String result = user.toString();

        assertThat(result).contains("providerId=null");
        assertThat(result).contains("projects=0");
        assertThat(result).contains("events=0");
    }

    @Test
    void toString_ShouldDisplaySummaryForPopulatedUser() {
        User user = new User();
        user.setId(1L);
        user.setProviderId("google-oauth2|12345");
        user.setProvider("google");
        user.setUsername("mockUser");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setCreatedAt(new Date());

        user.setProjects(List.of(new Project(), new Project(), new Project()));
        user.setEvents(List.of(new Event(), new Event(), new Event(), new Event(), new Event()));

        String result = user.toString();

        assertThat(result).contains("providerId=google-oauth2|12345");
        assertThat(result).contains("projects=3");
        assertThat(result).contains("events=5");
    }

}
