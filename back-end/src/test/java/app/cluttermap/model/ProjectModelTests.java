package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class ProjectModelTests {

    @Test
    void project_ShouldSetFieldsCorrectly_WhenConstructed() {
        /*
         * Test that Project's fields are correctly initialized when constructed with a
         * name and owner.
         * Verifies that name and owner are assigned correctly and that rooms is
         * initialized as an empty list.
         */
        User owner = new User("ownerProviderId");
        Project project = new Project("Test Project", owner);

        assertThat(project.getName()).isEqualTo("Test Project");
        assertThat(project.getOwner()).isEqualTo(owner);
        assertThat(project.getRooms()).isEmpty(); // Rooms should be empty on creation
    }

    @Test
    void project_ShouldManageRoomsCorrectly() {
        /*
         * Test that Project correctly manages its rooms collection in memory.
         * Verifies that adding and removing Room objects updates the collection as
         * expected, independent of database persistence.
         */
        User owner = new User("ownerProviderId");
        Project project = new Project("Test Project", owner);

        // Add a room
        Room room = new Room("Living Room", "This is the living room", project);
        project.getRooms().add(room);

        assertThat(project.getRooms()).hasSize(1);
        assertThat(project.getRooms().get(0).getName()).isEqualTo("Living Room");

        // Remove the room
        project.getRooms().remove(room);
        assertThat(project.getRooms()).isEmpty(); // Room should be removed
    }

}
