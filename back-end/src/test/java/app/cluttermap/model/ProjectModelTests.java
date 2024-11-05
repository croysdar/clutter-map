package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class ProjectModelTests {

    @Test
    void project_ShouldSetFieldsCorrectly_WhenConstructed() {
        // Arrange: Set up a user and create a project with a name and owner
        User owner = new User("ownerProviderId");

        // Act: Create a new Project instance
        Project project = new Project("Test Project", owner);

        // Assert: Verify that the project's fields are correctly set
        assertThat(project.getName()).isEqualTo("Test Project");
        assertThat(project.getOwner()).isEqualTo(owner);
        assertThat(project.getRooms()).isEmpty();
    }

    @Test
    void project_ShouldManageRoomsCorrectly() {
        // Arrange: Set up a user and create a project with an initial room
        User owner = new User("ownerProviderId");
        Project project = new Project("Test Project", owner);

        // Act: Add a room to the project
        Room room = new Room("Living Room", "This is the living room", project);
        project.getRooms().add(room);

        // Assert: Verify that the room was added to the project's rooms collection
        assertThat(project.getRooms()).hasSize(1);
        assertThat(project.getRooms().get(0).getName()).isEqualTo("Living Room");

        // Act: Remove the room from the project
        project.getRooms().remove(room);

        // Assert: Verify that the rooms collection is empty after removal
        assertThat(project.getRooms()).isEmpty();
    }

}
