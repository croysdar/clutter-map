package app.cluttermap.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import app.cluttermap.TestDataFactory;
import app.cluttermap.model.dto.RoomDTO;

public class RoomDTOTests {
    @Test
    void testRoomDTO_CorrectMapping() {
        Project project = new TestDataFactory.ProjectBuilder()
                .name("Project for Room")
                .user(new User())
                .build();

        Room room = new TestDataFactory.RoomBuilder()
                .project(project)
                .name("Test Room")
                .description("A room for testing")
                .build();

        OrgUnit orgUnit1 = new TestDataFactory.OrgUnitBuilder().id(1L).room(room).build();
        OrgUnit orgUnit2 = new TestDataFactory.OrgUnitBuilder().id(2L).room(room).build();

        // Convert to DTO
        RoomDTO roomDTO = new RoomDTO(room);

        // Assertions
        assertEquals(room.getId(), roomDTO.getId());
        assertEquals("Test Room", roomDTO.getName());
        assertEquals("A room for testing", roomDTO.getDescription());
        assertEquals(project.getId(), roomDTO.getProjectId());
        assertEquals(List.of(orgUnit1.getId(), orgUnit2.getId()), roomDTO.getOrgUnitIds());
    }

    @Test
    void testRoomDTO_EmptyListsHandledProperly() {
        // Create a project and room with no org units
        Project project = new TestDataFactory.ProjectBuilder()
                .user(new User())
                .build();

        Room room = new TestDataFactory.RoomBuilder()
                .project(project)
                .build();

        // Convert to DTO
        RoomDTO roomDTO = new RoomDTO(room);

        // Assertions
        assertEquals(room.getId(), roomDTO.getId());
        assertNotNull(roomDTO.getOrgUnitIds());
        assertTrue(roomDTO.getOrgUnitIds().isEmpty());
    }
}
