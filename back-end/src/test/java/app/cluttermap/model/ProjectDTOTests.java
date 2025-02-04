package app.cluttermap.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import app.cluttermap.TestDataFactory;
import app.cluttermap.model.dto.ProjectDTO;

public class ProjectDTOTests {

    @Test
    void testProjectDTO_CorrectMapping() {
        // Create test data using TestDataFactory
        Project project = new TestDataFactory.ProjectBuilder()
                .name("Test Project")
                .user(new User())
                .build();

        Room room1 = new TestDataFactory.RoomBuilder().project(project).id(1L).build();
        Room room2 = new TestDataFactory.RoomBuilder().project(project).id(2L).build();

        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().project(project).build();
        Item item = new TestDataFactory.ItemBuilder().project(project).build();

        // Convert to DTO
        ProjectDTO projectDTO = new ProjectDTO(project);

        // Assertions
        assertEquals(project.getId(), projectDTO.getId());
        assertEquals("Test Project", projectDTO.getName());
        assertEquals(List.of(room1.getId(), room2.getId()), projectDTO.getRoomIds());
        assertEquals(List.of(orgUnit.getId()), projectDTO.getOrgUnitIds());
        assertEquals(List.of(item.getId()), projectDTO.getItemIds());
    }

    @Test
    void testProjectDTO_EmptyListsHandledProperly() {
        // Create a project with explicitly empty lists
        Project project = new TestDataFactory.ProjectBuilder()
                .user(new User())
                .build();

        // Explicitly set empty lists
        project.setRooms(List.of());
        project.setOrgUnits(List.of());
        project.setItems(List.of());

        // Convert to DTO
        ProjectDTO projectDTO = new ProjectDTO(project);

        // Assertions
        assertEquals(project.getId(), projectDTO.getId());
        assertNotNull(projectDTO.getRoomIds());
        assertNotNull(projectDTO.getOrgUnitIds());
        assertNotNull(projectDTO.getItemIds());
        assertTrue(projectDTO.getRoomIds().isEmpty());
        assertTrue(projectDTO.getOrgUnitIds().isEmpty());
        assertTrue(projectDTO.getItemIds().isEmpty());
    }

}
