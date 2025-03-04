package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotSame;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.TestDataFactory;

@ActiveProfiles("test")
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
        Room room = new TestDataFactory.RoomBuilder().project(project).build();

        // Assert: Verify that the room was added to the project's rooms collection
        assertThat(project.getRooms()).hasSize(1);
        assertThat(project.getRooms().get(0).getProject()).isEqualTo(project);

        // Act: Remove the room from the project
        project.getRooms().remove(room);

        // Assert: Verify that the rooms collection is empty after removal
        assertThat(project.getRooms()).isEmpty();
    }

    @Test
    void toString_ShouldHandleNullFields() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Empty Project");

        String result = project.toString();

        assertThat(result).contains("owner=null");
        assertThat(result).contains("rooms=0");
        assertThat(result).contains("orgUnits=0");
        assertThat(result).contains("items=0");
        assertThat(result).contains("events=0");
    }

    @Test
    void toString_ShouldDisplaySummaryForFullProject() {
        User owner = new User("mockProviderId");
        owner.setUsername("mockUser");

        Project project = new Project();
        project.setId(1L);
        project.setName("Home Organization");
        project.setOwner(owner);

        // Add mock data for collections
        project.setRooms(List.of(new Room(), new Room(), new Room()));
        project.setOrgUnits(List.of(new OrgUnit(), new OrgUnit()));
        project.setItems(List.of(new Item(), new Item(), new Item(), new Item(), new Item()));
        project.setEvents(List.of(new Event(), new Event(), new Event()));

        String result = project.toString();

        assertThat(result).contains("owner=mockUser");
        assertThat(result).contains("rooms=3");
        assertThat(result).contains("orgUnits=2");
        assertThat(result).contains("items=5");
        assertThat(result).contains("events=3");
    }

    @Test
    void copyProject_ShouldProduceIdenticalCopy() {
        User user = new User("userProviderId");
        Project original = new TestDataFactory.ProjectBuilder()
                .id(1L)
                .name("Original Name")
                .user(user).build();

        Project copy = original.copy();
        // We don't copy the owner, but to use recursive to check equality,
        // it must be set
        copy.setOwner(user);

        // Assert that all fields are identical
        assertThat(copy).usingRecursiveComparison().isEqualTo(original);

        // Verify the copy is a new instance, not the same reference
        assertNotSame(copy, original);
    }
}
