package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.TestDataFactory;

@ActiveProfiles("test")
public class ItemModelTests {
    @Test
    void item_ShouldSetFieldsCorrectly_WhenConstructed() {
        // Arrange: Set up a user and create a project for the item
        User user = new User("userProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(user).build();
        Room room = new TestDataFactory.RoomBuilder().project(project).build();
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().room(room).build();

        // Act: Create a new Item instance
        Item item = new Item("Test Item", "Item Description", List.of("tag 1", "tag 2"), 10, orgUnit);

        // Assert: Verify that the items's fields are correctly set
        assertThat(item.getName()).isEqualTo("Test Item");
        assertThat(item.getDescription()).isEqualTo("Item Description");
        assertThat(item.getTags()).isEqualTo(List.of("tag 1", "tag 2"));
        assertThat(item.getOrgUnit()).isEqualTo(orgUnit);
        assertThat(item.getQuantity()).isEqualTo(10);
    }
}
