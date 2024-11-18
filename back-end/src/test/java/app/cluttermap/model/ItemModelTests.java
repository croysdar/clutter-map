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
        User owner = new User("ownerProviderId");
        Project project = new Project("Test Project", owner);
        Room room = new Room("Test Room", "Room Description", project);
        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", room);

        // Act: Create a new Item instance
        Item item = new TestDataFactory.ItemBuilder()
                .name("Test Item")
                .description("Item Description")
                .tags(List.of("tag 1", "tag 2"))
                .quantity(10)
                .orgUnit(orgUnit).build();

        // Assert: Verify that the items's fields are correctly set
        assertThat(item.getName()).isEqualTo("Test Item");
        assertThat(item.getDescription()).isEqualTo("Item Description");
        assertThat(item.getTags()).isEqualTo(List.of("tag 1", "tag 2"));
        assertThat(item.getOrgUnit()).isEqualTo(orgUnit);
        assertThat(item.getQuantity()).isEqualTo(10);
    }
}
