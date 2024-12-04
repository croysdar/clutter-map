package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.TestDataFactory;

@ActiveProfiles("test")
public class RoomModelTests {
    @Test
    void room_ShouldSetFieldsCorrectly_WhenConstructed() {
        // Arrange: Set up a user and create a project for the room
        User owner = new User("ownerProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(owner).build();

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
        Project project = new TestDataFactory.ProjectBuilder().user(owner).build();
        Room room = new Room("Test Room", "Room Description", project);

        // Act: Add a orgUnit to the room
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().room(room).build();
        room.getOrgUnits().add(orgUnit);

        // Assert: Verify that the orgUnit was added to the room's orgUnits
        // collection
        assertThat(room.getOrgUnits()).hasSize(1);
        assertThat(room.getOrgUnits().get(0).getRoom()).isEqualTo(room);

        // Act: Remove the orgUnit from the room
        room.getOrgUnits().remove(orgUnit);

        // Assert: Verify that the orgUnits collection is empty after removal
        assertThat(room.getOrgUnits()).isEmpty();
    }

    @Test
    void toString_ShouldHandleNullFields() {
        Room room = new Room();
        room.setId(1L);
        room.setName("Living Room");

        String result = room.toString();

        assertThat(result).contains("project=null");
        assertThat(result).contains("orgUnits=0");
    }

    @Test
    void toString_ShouldDisplaySummaryForPopulatedRoom() {
        Project project = new Project();
        project.setName("Home Project");

        Room room = new Room();
        room.setId(1L);
        room.setName("Living Room");
        room.setDescription("Main living area");
        room.setProject(project);

        room.setOrgUnits(List.of(new OrgUnit(), new OrgUnit(), new OrgUnit()));

        String result = room.toString();

        assertThat(result).contains("project=Home Project");
        assertThat(result).contains("orgUnits=3");
    }
}
