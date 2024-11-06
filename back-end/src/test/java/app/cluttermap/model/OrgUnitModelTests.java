package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

public class OrgUnitModelTests {
    @Test
    void orgUnit_ShouldSetFieldsCorrectly_WhenConstructed() {
        // Arrange: Set up a user and create a project for the orgUnit
        User owner = new User("ownerProviderId");
        Project project = new Project("Test Project", owner);
        Room room = new Room("Test Room", "Room Description", project);

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
        Project project = new Project("Test Project", owner);
        Room room = new Room("Test Room", "Room Description", project);
        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", room);

        // Act: Add a item to the orgUnit
        Item item = new Item("White Shelving Unit", "This is a shelving unit", List.of("tag1"), orgUnit);
        orgUnit.getItems().add(item);

        // Assert: Verify that the item was added to the orgUnit's items
        // collection
        assertThat(orgUnit.getItems()).hasSize(1);
        assertThat(orgUnit.getItems().get(0).getName()).isEqualTo("White Shelving Unit");
        assertThat(orgUnit.getItems().get(0).getTags()).isEqualTo(List.of("tag1"));
        assertThat(orgUnit.getItems().get(0).getOrgUnit()).isEqualTo(orgUnit);

        // Act: Remove the item from the orgUnit
        orgUnit.getItems().remove(item);

        // Assert: Verify that the items collection is empty after removal
        assertThat(orgUnit.getItems()).isEmpty();
    }

}
