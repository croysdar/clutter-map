package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotSame;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.TestDataFactory;

@ActiveProfiles("test")
public class OrgUnitModelTests {
    @Test
    void orgUnit_ShouldSetFieldsCorrectly_WhenConstructed() {
        // Arrange: Set up a user and create a project for the orgUnit
        User owner = new User("ownerProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(owner).build();
        Room room = new TestDataFactory.RoomBuilder().project(project).build();

        // Act: Create a new OrgUnit instance
        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", room);

        // Assert: Verify that the orgUnit's fields are correctly set
        assertThat(orgUnit.getName()).isEqualTo("Test OrgUnit");
        assertThat(orgUnit.getDescription()).isEqualTo("OrgUnit Description");
        assertThat(orgUnit.getRoom()).isEqualTo(room);
        assertThat(orgUnit.getItems()).isEmpty();
    }

    @Test
    void orgUnit_ShouldManageItemsCorrectly() {
        // Arrange: Set up a user, create a project, and a orgUnit
        User owner = new User("ownerProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(owner).build();
        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", room);

        // Act: Add a item to the orgUnit
        Item item = new TestDataFactory.ItemBuilder().id(1L).orgUnit(orgUnit).build();

        // Assert: Verify that the item was added to the orgUnit's items
        // collection
        assertThat(orgUnit.getItems()).hasSize(1);
        assertThat(orgUnit.getItems().get(0).getName()).isEqualTo(item.getName());
        assertThat(orgUnit.getItems().get(0).getOrgUnit()).isEqualTo(orgUnit);

        // Act: Remove the item from the orgUnit
        orgUnit.getItems().remove(item);

        // Assert: Verify that the items collection is empty after removal
        assertThat(orgUnit.getItems()).isEmpty();
    }

    @Test
    void toString_ShouldHandleNullFields() {
        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setId(1L);
        orgUnit.setName("Kitchen Shelf");

        String result = orgUnit.toString();

        assertThat(result).contains("roomId=null");
        assertThat(result).contains("projectId=null");
    }

    @Test
    void toString_ShouldDisplaySummaryForFullOrgUnit() {
        // Arrange
        Project project = new TestDataFactory.ProjectBuilder().id(5L).user(new User("")).build();

        Room room = new TestDataFactory.RoomBuilder().id(20L).project(project).build();

        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder()
                .id(1L)
                .name("Shelf")
                .description("A wooden shelf in the living room")
                .room(room)
                .build();

        orgUnit.setItems(List.of(new Item(), new Item(), new Item())); // 3 items

        // Act
        String result = orgUnit.toString();

        // Assert
        assertThat(result).contains("id=1");
        assertThat(result).contains("name='Shelf'");
        assertThat(result).contains("description='A wooden shelf in the living room'");
        assertThat(result).contains("projectId=5");
        assertThat(result).contains("roomId=20");
    }

    @Test
    void copyOrgUnit_ShouldProduceIdenticalCopy() {
        User user = new User("userProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(user).build();
        OrgUnit original = new TestDataFactory.OrgUnitBuilder()
                .id(1L)
                .name("Original Name")
                .description("Original Description")
                .project(project)
                .build();

        OrgUnit copy = original.copy();

        // Assert that all fields are identical
        assertThat(copy).usingRecursiveComparison().isEqualTo(original);

        // Verify the copy is a new instance, not the same reference
        assertNotSame(copy, original);
    }
}
