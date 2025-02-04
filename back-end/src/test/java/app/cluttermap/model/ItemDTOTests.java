package app.cluttermap.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import app.cluttermap.TestDataFactory;
import app.cluttermap.model.dto.ItemDTO;

public class ItemDTOTests {

    @Test
    void testItemDTO_CorrectMapping() {
        // Create test data using TestDataFactory
        Project project = new TestDataFactory.ProjectBuilder()
                .user(new User())
                .build();

        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder()
                .project(project)
                .name("Storage Bin")
                .build();

        Item item = new TestDataFactory.ItemBuilder()
                .orgUnit(orgUnit)
                .name("Flashlight")
                .description("A bright LED flashlight")
                .tags(List.of("camping", "light"))
                .quantity(2)
                .build();

        // Convert to DTO
        ItemDTO itemDTO = new ItemDTO(item);

        // Assertions
        assertEquals(item.getId(), itemDTO.getId());
        assertEquals("Flashlight", itemDTO.getName());
        assertEquals("A bright LED flashlight", itemDTO.getDescription());
        assertEquals(List.of("camping", "light"), itemDTO.getTags());
        assertEquals(2, itemDTO.getQuantity());
        assertTrue(itemDTO.getOrgUnitId().isPresent());
        assertEquals(orgUnit.getId(), itemDTO.getOrgUnitId().get());
        assertTrue(itemDTO.getOrgUnitName().isPresent());
        assertEquals(orgUnit.getName(), itemDTO.getOrgUnitName().get());
        assertEquals(project.getId(), itemDTO.getProjectId());
    }

    @Test
    void testItemDTO_EmptyTagsHandledProperly() {
        // Create a project and item with empty tags
        Project project = new TestDataFactory.ProjectBuilder()
                .user(new User())
                .build();

        Item item = new TestDataFactory.ItemBuilder()
                .project(project)
                .name("Lantern")
                .tags(List.of()) // Empty list
                .build();

        // Convert to DTO
        ItemDTO itemDTO = new ItemDTO(item);

        // Assertions
        assertEquals(item.getId(), itemDTO.getId());
        assertNotNull(itemDTO.getTags());
        assertTrue(itemDTO.getTags().isEmpty());
    }

    @Test
    void testItemDTO_NullOrgUnit() {
        // Create a project and an item with no org unit
        Project project = new TestDataFactory.ProjectBuilder()
                .name("No OrgUnit Project")
                .user(new User())
                .build();

        Item item = new TestDataFactory.ItemBuilder()
                .project(project)
                .name("Notebook")
                .description("A blank notebook")
                .quantity(1)
                .build();

        // Convert to DTO
        ItemDTO itemDTO = new ItemDTO(item);

        // Assertions
        assertEquals(item.getId(), itemDTO.getId());
        assertEquals("Notebook", itemDTO.getName());
        assertEquals("A blank notebook", itemDTO.getDescription());
        assertEquals(1, itemDTO.getQuantity());
        assertTrue(itemDTO.getOrgUnitId().isEmpty());
        assertTrue(itemDTO.getOrgUnitName().isEmpty());
        assertEquals(project.getId(), itemDTO.getProjectId());
    }
}
