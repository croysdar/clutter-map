package app.cluttermap.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.cluttermap.exception.org_unit.OrgUnitNotFoundException;
import app.cluttermap.exception.room.RoomNotFoundException;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewOrgUnitDTO;
import app.cluttermap.model.dto.UpdateOrgUnitDTO;
import app.cluttermap.service.OrgUnitService;
import app.cluttermap.service.RoomService;
import app.cluttermap.service.SecurityService;

@WebMvcTest(OrgUnitController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class OrgUnitControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @MockBean
    private OrgUnitService orgUnitService;

    @MockBean
    private SecurityService securityService;

    private User mockUser;
    private Project mockProject;
    private Room mockRoom;

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
        mockProject = new Project("Mock Project", mockUser);
        mockRoom = new Room("Mock Room", "Room Description", mockProject);

        when(securityService.getCurrentUser()).thenReturn(mockUser);
        // Stub isResourceOwner to allow access to protected resources
        when(securityService.isResourceOwner(anyLong(), eq("room"))).thenReturn(true);
        when(securityService.isResourceOwner(anyLong(), eq("org-unit"))).thenReturn(true);
        when(securityService.isResourceOwner(anyLong(), eq("project"))).thenReturn(true);
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getUserOrgUnits_ShouldReturnAllUserOrgUnits() throws Exception {
        // Arrange: Set up mock user orgUnits and mock the service to return them
        OrgUnit orgUnit1 = new OrgUnit("Test OrgUnit 1", "Description 1", mockRoom);
        OrgUnit orgUnit2 = new OrgUnit("Test OrgUnit 2", "Description 2", mockRoom);
        when(orgUnitService.getUserOrgUnits()).thenReturn(List.of(orgUnit1, orgUnit2));

        // Act: Perform a GET request to the /org-units endpoint
        mockMvc.perform(get("/org-units"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected orgUnit names
                .andExpect(jsonPath("$[0].name").value("Test OrgUnit 1"))
                .andExpect(jsonPath("$[0].description").value("Description 1"))
                .andExpect(jsonPath("$[1].name").value("Test OrgUnit 2"))
                .andExpect(jsonPath("$[1].description").value("Description 2"));

        // Assert: Ensure that the service method was called
        verify(orgUnitService).getUserOrgUnits();
    }

    @Test
    void getUserOrgUnits_ShouldReturnEmptyList_WhenNoOrgUnitsExist() throws Exception {
        // Arrange: Set up the service to return an empty list
        when(orgUnitService.getUserOrgUnits()).thenReturn(Collections.emptyList());

        // Act: Perform a GET request to the /org-units endpoint
        mockMvc.perform(get("/org-units"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains an empty array
                .andExpect(jsonPath("$").isEmpty());

        // Assert: Ensure that the service method was called
        verify(orgUnitService).getUserOrgUnits();
    }

    @Test
    void getOneOrgUnit_ShouldReturnOrgUnit_WhenOrgUnitExists() throws Exception {
        // Arrange: Set up a mock orgUnit and stub the service to return it when
        // searched by ID
        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit description", mockRoom);
        when(orgUnitService.getOrgUnitById(1L)).thenReturn(orgUnit);

        // Act: Perform a GET request to the /org-units/1 endpoint
        mockMvc.perform(get("/org-units/1"))
                .andExpect(status().isOk())
                // Assert: Verify the response contains the expected orgUnit name
                .andExpect(jsonPath("$.name").value("Test OrgUnit"))
                .andExpect(jsonPath("$.description").value("OrgUnit description"));

        // Assert: Ensure that the service method was called
        verify(orgUnitService).getOrgUnitById(1L);
    }

    @Test
    void getOneOrgUnit_ShouldReturnNotFound_WhenOrgUnitDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw OrgUnitNotFoundException when a
        // non-existent orgUnit ID is requested
        when(orgUnitService.getOrgUnitById(1L)).thenThrow(new OrgUnitNotFoundException());

        // Act: Perform a GET request to the /org-units/1 endpoint
        mockMvc.perform(get("/org-units/1"))
                .andExpect(status().isNotFound());

        // Assert: Ensure that the service method was called
        verify(orgUnitService).getOrgUnitById(1L);
    }

    @Test
    void addOneOrgUnit_ShouldCreateOrgUnit_WhenValidRequest() throws Exception {
        // Arrange: Set up a NewOrgUnitDTO with valid data and mock the service to
        // return a new orgUnit
        NewOrgUnitDTO orgUnitDTO = new NewOrgUnitDTO("New OrgUnit", "OrgUnit Description", String.valueOf(1L));
        OrgUnit newOrgUnit = new OrgUnit(orgUnitDTO.getName(), orgUnitDTO.getDescription(), mockRoom);
        when(orgUnitService.createOrgUnit(any(NewOrgUnitDTO.class))).thenReturn(newOrgUnit);

        // Act: Perform a POST request to the /org-units endpoint with the orgUnit data
        mockMvc.perform(post("/org-units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected orgUnit name
                .andExpect(jsonPath("$.name").value("New OrgUnit"))
                .andExpect(jsonPath("$.description").value("OrgUnit Description"));

        // Assert: Ensure the service method was called to create the orgUnit
        verify(orgUnitService).createOrgUnit(any(NewOrgUnitDTO.class));
    }

    @Test
    void addOneOrgUnit_ShouldReturnBadRequest_WhenOrgUnitNameIsBlank() throws Exception {
        // Arrange: Set up a NewOrgUnitDTO with a blank name to trigger validation
        NewOrgUnitDTO orgUnitDTO = new NewOrgUnitDTO("", "", String.valueOf(1L));

        // Act: Perform a POST request to the /org-units endpoint with the blank orgUnit
        // name
        mockMvc.perform(post("/org-units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Organization unit name must not be blank."));
    }

    @Test
    void addOneOrgUnit_ShouldReturnBadRequest_WhenOrgUnitNameIsNull() throws Exception {
        // Arrange: Set up a NewOrgUnitDTO with a null name to trigger validation
        NewOrgUnitDTO orgUnitDTO = new NewOrgUnitDTO(null, "", String.valueOf(1L));

        // Act: Perform a POST request to the /org-units endpoint with the null orgUnit
        // name
        mockMvc.perform(post("/org-units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Organization unit name must not be blank."));
    }

    @Test
    void addOneOrgUnit_ShouldReturnBadRequest_WhenOrgUnitIdIsNull() throws Exception {
        // Arrange: Set up a NewOrgUnitDTO with a null orgUnit ID to trigger validation
        NewOrgUnitDTO orgUnitDTO = new NewOrgUnitDTO("OrgUnit Name", "", null);

        // Act: Perform a POST request to the /org-units endpoint with the null orgUnit
        // ID
        mockMvc.perform(post("/org-units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("roomId"))
                .andExpect(jsonPath("$.errors[0].message").value("Room ID must not be blank."));
    }

    @Test
    void addOneOrgUnit_ShouldReturnBadRequest_WhenOrgUnitIdIsNaN() throws Exception {
        // Arrange: Set up a NewOrgUnitDTO with a NaN orgUnit ID to trigger validation
        NewOrgUnitDTO orgUnitDTO = new NewOrgUnitDTO("OrgUnit Name", "", "string");

        // Act: Perform a POST request to the /org-units endpoint with the NaN orgUnit
        // ID
        mockMvc.perform(post("/org-units")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("roomId"))
                .andExpect(jsonPath("$.errors[0].message").value("Room ID must be a valid number."));
    }

    @Test
    void updateOneOrgUnit_ShouldUpdateOrgUnit_WhenValidRequest() throws Exception {
        // Arrange: Set up an UpdateOrgUnitDTO with a new name and mock the service to
        // return the updated orgUnit
        UpdateOrgUnitDTO orgUnitDTO = new UpdateOrgUnitDTO("Updated OrgUnit", "Updated Description");
        OrgUnit updatedOrgUnit = new OrgUnit(orgUnitDTO.getName(), orgUnitDTO.getDescription(), mockRoom);
        when(orgUnitService.updateOrgUnit(eq(1L), any(UpdateOrgUnitDTO.class))).thenReturn(updatedOrgUnit);

        // Act: Perform a PUT request to the /org-units/1 endpoint with the update data
        mockMvc.perform(put("/org-units/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the updated orgUnit name
                .andExpect(jsonPath("$.name").value("Updated OrgUnit"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        // Assert: Ensure the service method was called
        verify(orgUnitService).updateOrgUnit(eq(1L), any(UpdateOrgUnitDTO.class));
    }

    @Test
    void updateOneOrgUnit_ShouldReturnBadRequest_WhenOrgUnitNameIsBlank() throws Exception {
        // Arrange: Set up an UpdateOrgUnitDTO with a blank orgUnit name to trigger
        // validation
        UpdateOrgUnitDTO orgUnitDTO = new UpdateOrgUnitDTO("", "Updated Description");

        // Act: Perform a PUT request to the /org-units/1 endpoint with the invalid
        // orgUnit name
        mockMvc.perform(put("/org-units/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Organization unit name must not be blank."));
    }

    @Test
    void updateOneOrgUnit_ShouldReturnBadRequest_WhenOrgUnitNameIsNull() throws Exception {
        // Arrange: Set up an UpdateOrgUnitDTO with a null orgUnit name to trigger
        // validation
        UpdateOrgUnitDTO orgUnitDTO = new UpdateOrgUnitDTO(null, "Updated Description");

        // Act: Perform a PUT request to the /org-units/1 endpoint with the null orgUnit
        // name
        mockMvc.perform(put("/org-units/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Organization unit name must not be blank."));
    }

    @Test
    void moveOrgUnit_Success() throws Exception {
        // Arrange: Set up orgUnitId, targetRoomId, and simulate a successful move.
        Long orgUnitId = 1L;
        Long targetRoomId = 2L;
        OrgUnit orgUnit = new OrgUnit("Test OrgUnit", "OrgUnit Description", mockRoom);

        when(orgUnitService.moveOrgUnitBetweenRooms(orgUnitId, targetRoomId)).thenReturn(orgUnit);

        // Act & Assert: Perform the PUT request and verify status 200 OK and correct
        // orgUnit data.
        mockMvc.perform(put("/org-units/{orgUnitId}/move-room/{roomId}", orgUnitId, targetRoomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test OrgUnit"));
    }

    @Test
    void moveOrgUnit_DifferentProjects_ShouldReturnBadRequest() throws Exception {
        // Arrange: Set up orgUnitId, targetRoomId, and simulate service throwing
        // IllegalArgumentException.
        Long orgUnitId = 1L;
        Long targetRoomId = 2L;

        when(orgUnitService.moveOrgUnitBetweenRooms(orgUnitId, targetRoomId))
                .thenThrow(new IllegalArgumentException("Cannot move org unit to a different project's Room"));

        // Act & Assert: Perform the PUT request and verify status 400 Bad Request and
        // error message.
        mockMvc.perform(put("/org-units/{orgUnitId}/move-room/{roomId}", orgUnitId, targetRoomId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Cannot move org unit to a different project's Room"));
    }

    @Test
    void moveOrgUnit_OrgUnitNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange: Set up orgUnitId, targetRoomId, and simulate service throwing
        // OrgUnitNotFoundException.
        Long orgUnitId = 1L;
        Long targetRoomId = 2L;

        when(orgUnitService.moveOrgUnitBetweenRooms(orgUnitId, targetRoomId))
                .thenThrow(new OrgUnitNotFoundException());

        // Act & Assert: Perform the PUT request and verify status 404 Not Found.
        mockMvc.perform(put("/org-units/{orgUnitId}/move-room/{roomId}", orgUnitId, targetRoomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void moveOrgUnit_RoomNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange: Set up orgUnitId, targetRoomId, and simulate service throwing
        // RoomNotFoundException.
        Long orgUnitId = 1L;
        Long targetRoomId = 2L;

        when(orgUnitService.moveOrgUnitBetweenRooms(orgUnitId, targetRoomId))
                .thenThrow(new RoomNotFoundException());

        // Act & Assert: Perform the PUT request and verify status 404 Not Found.
        mockMvc.perform(put("/org-units/{orgUnitId}/move-room/{roomId}", orgUnitId, targetRoomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOneOrgUnit_ShouldDeleteOrgUnit_WhenOrgUnitExists() throws Exception {
        // Act: Perform a DELETE request to the /org-units/1 endpoint
        mockMvc.perform(delete("/org-units/1"))
                .andExpect(status().isNoContent());

        // Assert: Ensure the service method was called to delete the orgUnit by ID
        verify(orgUnitService).deleteOrgUnit(1L);
    }

    @Test
    void deleteOneOrgUnit_ShouldReturnNotFound_WhenOrgUnitDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw OrgUnitNotFoundException when deleting a
        // non-existent orgUnit
        doThrow(new OrgUnitNotFoundException()).when(orgUnitService).deleteOrgUnit(1L);

        // Act: Perform a DELETE request to the /org-units/1 endpoint
        mockMvc.perform(delete("/org-units/1"))
                .andExpect(status().isNotFound());

        // Assert: Ensure the service method was called to attempt to delete the orgUnit
        verify(orgUnitService).deleteOrgUnit(1L);
    }

    @Test
    void getOrgUnitItems_ShouldReturnItems_WhenOrgUnitExists() throws Exception {
        // Arrange: Set up a orgUnit with an item and mock the service to return the
        // orgUnit
        OrgUnit orgUnit = new OrgUnit("OrgUnit", "OrgUnit Description", mockRoom);
        Item item = new Item("Item", "Item Description", List.of("tag1"), orgUnit);
        orgUnit.setItems(Collections.singletonList(item));
        when(orgUnitService.getOrgUnitById(1L)).thenReturn(orgUnit);

        // Act: Perform a GET request to the /org-units/1/org-units endpoint
        mockMvc.perform(get("/org-units/1/items"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected orgUnit name
                .andExpect(jsonPath("$[0].name").value("Item"))
                .andExpect(jsonPath("$[0].description").value("Item Description"));

        // Assert: Ensure the service method was called to retrieve the orgUnit by ID
        verify(orgUnitService).getOrgUnitById(1L);
    }

    @Test
    void getOrgUnitItems_ShouldReturnNotFound_WhenOrgUnitDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw OrgUnitNotFoundException when retrieving
        // orgUnits for a non-existent orgUnit
        when(orgUnitService.getOrgUnitById(1L)).thenThrow(new OrgUnitNotFoundException());

        // Act: Perform a GET request to the /org-units/1/org-units endpoint
        mockMvc.perform(get("/org-units/1/items"))
                .andExpect(status().isNotFound());

        // Assert: Ensure the service method was called to attempt to retrieve the
        // orgUnit
        verify(orgUnitService).getOrgUnitById(1L);
    }
}
