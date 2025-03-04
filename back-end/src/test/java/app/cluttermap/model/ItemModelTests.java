package app.cluttermap.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotSame;

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

    @Test
    void toString_ShouldHandleNullFields() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Pizza Cutter");

        String result = item.toString();

        assertThat(result).contains("orgUnitId=null");
        assertThat(result).contains("projectId=null");
    }

    @Test
    void toString_ShouldDisplaySummaryForFullItem() {
        // Arrange
        Project project = new TestDataFactory.ProjectBuilder()
                .id(7L)
                .user(new User("userProviderId"))
                .build();

        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder()
                .id(15L)
                .project(project)
                .build();

        Item item = new TestDataFactory.ItemBuilder()
                .id(3L)
                .name("Pizza Cutter")
                .description("A sharp tool for slicing pizza")
                .tags(List.of("Kitchen", "Utensils"))
                .quantity(1)
                .orgUnit(orgUnit)
                .build();

        // Act
        String result = item.toString();

        // Assert
        assertThat(result).contains("id=3");
        assertThat(result).contains("name='Pizza Cutter'");
        assertThat(result).contains("description='A sharp tool for slicing pizza'");
        assertThat(result).contains("tags=[Kitchen, Utensils]");
        assertThat(result).contains("quantity=1");
        assertThat(result).contains("orgUnitId=15");
        assertThat(result).contains("projectId=7");
    }

    @Test
    void copyItem_ShouldProduceIdenticalCopy() {
        User user = new User("userProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(user).build();
        Item original = new TestDataFactory.ItemBuilder()
                .name("Original Name")
                .description("Original Description")
                .tags(List.of("Tag1", "Tag2"))
                .quantity(5)
                .project(project)
                .build();

        Item copy = original.copy();

        // Assert that all fields are identical
        assertThat(copy).usingRecursiveComparison().isEqualTo(original);

        // Verify the copy is a new instance, not the same reference
        assertNotSame(copy, original);
    }
}
