package app.cluttermap.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import app.cluttermap.TestDataFactory;
import app.cluttermap.model.dto.OrgUnitDTO;

public class OrgUnitDTOTests {

    @Test
    void testOrgUnitDTO_CorrectMapping() {
        // Create test data using TestDataFactory
        Project project = new TestDataFactory.ProjectBuilder()
                .user(new User())
                .build();

        Room room = new TestDataFactory.RoomBuilder()
                .project(project)
                .name("Parent Room")
                .build();

        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder()
                .room(room)
                .name("Test OrgUnit")
                .description("An organizational unit")
                .build();

        Item item1 = new TestDataFactory.ItemBuilder().orgUnit(orgUnit).id(1L).build();
        Item item2 = new TestDataFactory.ItemBuilder().orgUnit(orgUnit).id(2L).build();

        // Convert to DTO
        OrgUnitDTO orgUnitDTO = new OrgUnitDTO(orgUnit);

        // Assertions
        assertEquals(orgUnit.getId(), orgUnitDTO.getId());
        assertEquals("Test OrgUnit", orgUnitDTO.getName());
        assertEquals("An organizational unit", orgUnitDTO.getDescription());
        assertTrue(orgUnitDTO.getRoomId().isPresent());
        assertEquals(room.getId(), orgUnitDTO.getRoomId().get());
        assertTrue(orgUnitDTO.getRoomName().isPresent());
        assertEquals(room.getName(), orgUnitDTO.getRoomName().get());
        assertEquals(project.getId(), orgUnitDTO.getProjectId());
        assertEquals(List.of(item1.getId(), item2.getId()), orgUnitDTO.getItemIds());
    }

    @Test
    void testOrgUnitDTO_EmptyListsHandledProperly() {
        // Create a project and org unit with no items
        Project project = new TestDataFactory.ProjectBuilder()
                .user(new User())
                .build();

        Room room = new TestDataFactory.RoomBuilder()
                .project(project)
                .build();

        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder()
                .room(room)
                .project(project)
                .build();

        // Convert to DTO
        OrgUnitDTO orgUnitDTO = new OrgUnitDTO(orgUnit);

        // Assertions
        assertEquals(orgUnit.getId(), orgUnitDTO.getId());
        assertNotNull(orgUnitDTO.getItemIds());
        assertTrue(orgUnitDTO.getItemIds().isEmpty());
    }
}