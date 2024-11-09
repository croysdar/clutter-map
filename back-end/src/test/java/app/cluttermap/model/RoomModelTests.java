package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class RoomModelTests {
    @Test
    void room_ShouldSetFieldsCorrectly_WhenConstructed() {
        // Arrange: Set up a user and create a project for the room
        User owner = new User("ownerProviderId");
        Project project = new Project("Test Project", owner);

        // Act: Create a new Room instance
        Room room = new Room("Test Room", "Room Description", project);

        // Assert: Verify that the room's fields are correctly set
        assertThat(room.getName()).isEqualTo("Test Room");
        assertThat(room.getDescription()).isEqualTo("Room Description");
        assertThat(room.getProject()).isEqualTo(project);
        assertThat(room.getOrgUnits()).isEmpty();
    }

    @Test
    void room_ShouldManageOrgUnitsCorrectly() {
        // Arrange: Set up a user, create a project, and a room
        User owner = new User("ownerProviderId");
        Project project = new Project("Test Project", owner);
        Room room = new Room("Test Room", "Room Description", project);

        // Act: Add a orgUnit to the room
        OrgUnit orgUnit = new OrgUnit("White Shelving Unit", "This is a shelving unit", room);
        room.getOrgUnits().add(orgUnit);

        // Assert: Verify that the orgUnit was added to the room's orgUnits
        // collection
        assertThat(room.getOrgUnits()).hasSize(1);
        assertThat(room.getOrgUnits().get(0).getName()).isEqualTo("White Shelving Unit");
        assertThat(room.getOrgUnits().get(0).getRoom()).isEqualTo(room);

        // Act: Remove the orgUnit from the room
        room.getOrgUnits().remove(orgUnit);

        // Assert: Verify that the orgUnits collection is empty after removal
        assertThat(room.getOrgUnits()).isEmpty();
    }
}
