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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import app.cluttermap.TestDataFactory;
import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;
import app.cluttermap.service.OrgUnitService;
import app.cluttermap.service.RoomService;
import app.cluttermap.service.SecurityService;
import app.cluttermap.util.ResourceType;

@WebMvcTest(RoomController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class RoomControllerTests {

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

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
        mockProject = new Project("Mock Project", mockUser);

        when(securityService.getCurrentUser()).thenReturn(mockUser);
        // Stub isResourceOwner to allow access to protected resources
        when(securityService.isResourceOwner(anyLong(), eq("room"))).thenReturn(true);
        when(securityService.isResourceOwner(anyLong(), eq("project"))).thenReturn(true);
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getUserRooms_ShouldReturnAllUserRooms() throws Exception {
        // Arrange: Set up mock user rooms and mock the service to return them
        Room room1 = new Room("Test Room 1", "Description 1", mockProject);
        Room room2 = new Room("Test Room 2", "Description 2", mockProject);
        when(roomService.getUserRooms()).thenReturn(List.of(room1, room2));

        // Act: Perform a GET request to the /rooms endpoint
        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected room names
                .andExpect(jsonPath("$[0].name").value("Test Room 1"))
                .andExpect(jsonPath("$[0].description").value("Description 1"))
                .andExpect(jsonPath("$[1].name").value("Test Room 2"))
                .andExpect(jsonPath("$[1].description").value("Description 2"));

        // Assert: Ensure that the service method was called
        verify(roomService).getUserRooms();
    }

    @Test
    void getUserRooms_ShouldReturnEmptyList_WhenNoRoomsExist() throws Exception {
        // Arrange: Set up the service to return an empty list
        when(roomService.getUserRooms()).thenReturn(Collections.emptyList());

        // Act: Perform a GET request to the /rooms endpoint
        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains an empty array
                .andExpect(jsonPath("$").isEmpty());

        // Assert: Ensure that the service method was called
        verify(roomService).getUserRooms();
    }

    @Test
    void getOneRoom_ShouldReturnRoom_WhenRoomExists() throws Exception {
        // Arrange: Set up a mock room and stub the service to return it when
        // searched by ID
        Room room = new Room("Test Room", "Room description", mockProject);
        when(roomService.getRoomById(1L)).thenReturn(room);

        // Act: Perform a GET request to the /rooms/1 endpoint
        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isOk())
                // Assert: Verify the response contains the expected room name
                .andExpect(jsonPath("$.name").value("Test Room"));

        // Assert: Ensure that the service method was called
        verify(roomService).getRoomById(1L);
    }

    @Test
    void getOneRoom_ShouldReturnNotFound_WhenRoomDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw RoomNotFoundException when a
        // non-existent room ID is requested
        when(roomService.getRoomById(1L)).thenThrow(new ResourceNotFoundException(ResourceType.ROOM, 1L));

        // Act: Perform a GET request to the /rooms/1 endpoint
        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("ROOM with ID 1 not found."));

        // Assert: Ensure that the service method was called
        verify(roomService).getRoomById(1L);
    }

    @Test
    void addOneRoom_ShouldCreateRoom_WhenValidRequest() throws Exception {
        // Arrange: Set up a NewRoomDTO with valid data and mock the service to
        // return a new room
        NewRoomDTO roomDTO = new TestDataFactory.NewRoomDTOBuilder().build();
        Room newRoom = new Room(roomDTO.getName(), roomDTO.getDescription(), mockProject);
        when(roomService.createRoom(any(NewRoomDTO.class))).thenReturn(newRoom);

        // Act: Perform a POST request to the /rooms endpoint with the room data
        mockMvc.perform(post("/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected room name
                .andExpect(jsonPath("$.name").value(roomDTO.getName()));

        // Assert: Ensure the service method was called to create the room
        verify(roomService).createRoom(any(NewRoomDTO.class));
    }

    @Test
    void addOneRoom_ShouldReturnBadRequest_WhenRoomNameIsBlank() throws Exception {
        // Arrange: Set up a NewRoomDTO with a blank name to trigger validation
        NewRoomDTO roomDTO = new TestDataFactory.NewRoomDTOBuilder().name("").build();

        // Act: Perform a POST request to the /rooms endpoint with the blank room
        // name
        mockMvc.perform(post("/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Room name must not be blank."));
    }

    @Test
    void addOneRoom_ShouldReturnBadRequest_WhenRoomNameIsNull() throws Exception {
        // Arrange: Set up a NewRoomDTO with a null name to trigger validation
        NewRoomDTO roomDTO = new TestDataFactory.NewRoomDTOBuilder().name(null).build();

        // Act: Perform a POST request to the /rooms endpoint with the null room name
        mockMvc.perform(post("/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Room name must not be blank."));
    }

    @Test
    void addOneRoom_ShouldReturnBadRequest_WhenProjectIdIsNull() throws Exception {
        // Arrange: Set up a NewRoomDTO with a null project ID to trigger validation
        NewRoomDTO roomDTO = new TestDataFactory.NewRoomDTOBuilder().projectId(null).build();

        // Act: Perform a POST request to the /rooms endpoint with the null project ID
        mockMvc.perform(post("/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("projectId"))
                .andExpect(jsonPath("$.errors[0].message").value("Project ID must not be blank."));
    }

    @Test
    void addOneRoom_ShouldReturnBadRequest_WhenProjectIdIsNaN() throws Exception {
        // Arrange: Set up a NewRoomDTO with a NaN project ID to trigger validation
        NewRoomDTO roomDTO = new TestDataFactory.NewRoomDTOBuilder().projectId("invalid").build();

        // Act: Perform a POST request to the /rooms endpoint with the NaN project ID
        mockMvc.perform(post("/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("projectId"))
                .andExpect(jsonPath("$.errors[0].message").value("Project ID must be a valid number."));
    }

    @Test
    void updateOneRoom_ShouldUpdateRoom_WhenValidRequest() throws Exception {
        // Arrange: Set up an UpdateRoomDTO with a new name and mock the service to
        // return the updated room
        UpdateRoomDTO roomDTO = new TestDataFactory.UpdateRoomDTOBuilder().build();

        Room updatedRoom = new Room(roomDTO.getName(), roomDTO.getDescription(), mockProject);
        when(roomService.updateRoom(eq(1L), any(UpdateRoomDTO.class))).thenReturn(updatedRoom);

        // Act: Perform a PUT request to the /rooms/1 endpoint with the update data
        mockMvc.perform(put("/rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the updated room name
                .andExpect(jsonPath("$.name").value(roomDTO.getName()))
                .andExpect(jsonPath("$.description").value(roomDTO.getDescription()));

        // Assert: Ensure the service method was called
        verify(roomService).updateRoom(eq(1L), any(UpdateRoomDTO.class));
    }

    @Test
    void updateOneRoom_ShouldReturnBadRequest_WhenRoomNameIsBlank() throws Exception {
        // Arrange: Set up an UpdateRoomDTO with a blank room name to trigger
        // validation
        UpdateRoomDTO roomDTO = new TestDataFactory.UpdateRoomDTOBuilder().name("").build();

        // Act: Perform a PUT request to the /rooms/1 endpoint with the invalid
        // room name
        mockMvc.perform(put("/rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Room name must not be blank."));
    }

    @Test
    void updateOneRoom_ShouldReturnBadRequest_WhenRoomNameIsNull() throws Exception {
        // Arrange: Set up an UpdateRoomDTO with a null room name to trigger
        // validation
        UpdateRoomDTO roomDTO = new TestDataFactory.UpdateRoomDTOBuilder().name(null).build();

        // Act: Perform a PUT request to the /rooms/1 endpoint with the null room
        // name
        mockMvc.perform(put("/rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Room name must not be blank."));
    }

    @Test
    void assignOrgUnitsToRoom_Success() throws Exception {
        // Arrange: Set up orgUnitIds and Room ID
        List<Long> orgUnitIds = List.of(1L, 2L, 3L);
        Long targetRoomId = 10L;

        Room targetRoom = new Room("Target Room", "Description", mockProject);

        List<String> orgUnitNames = List.of("OrgUnit 1", "OrgUnit 2", "OrgUnit 3");
        List<OrgUnit> movedOrgUnits = List.of(
                new TestDataFactory.OrgUnitBuilder().name(orgUnitNames.get(0)).room(targetRoom).build(),
                new TestDataFactory.OrgUnitBuilder().name(orgUnitNames.get(1)).room(targetRoom).build(),
                new TestDataFactory.OrgUnitBuilder().name(orgUnitNames.get(2)).room(targetRoom).build());

        when(orgUnitService.assignOrgUnitsToRoom(orgUnitIds, targetRoomId))
                .thenReturn(movedOrgUnits);

        // Act & Assert: Perform PUT request and verify status 200 OK with updated org
        // Units
        mockMvc.perform(put("/rooms/{targetRoomId}/org-units", targetRoomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(orgUnitNames.get(0)))
                .andExpect(jsonPath("$[1].name").value(orgUnitNames.get(1)))
                .andExpect(jsonPath("$[2].name").value(orgUnitNames.get(2)));
    }

    @Test
    void assignOrgUnitsToRoom_TargetRoomNotFound_ShouldReturnNotFound()
            throws Exception {
        // Arrange: Set up orgUnitIds and non existent Room ID
        List<Long> orgUnitIds = List.of(1L, 2L, 3L);
        Long targetRoomId = 999L;

        when(orgUnitService.assignOrgUnitsToRoom(orgUnitIds, targetRoomId))
                .thenThrow(new ResourceNotFoundException(ResourceType.ROOM, 999L));

        // Act & Assert: Perform PUT request and verify status 404 Not Found
        mockMvc.perform(put("/rooms/{targetRoomId}/org-units", targetRoomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitIds)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("ROOM with ID 999 not found."));
    }

    // TODO allow partial success
    @Test
    void assignOrgUnitsToRoom_OrgUnitNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange: Set up Room ID and list with a non-existent Org Unit ID
        List<Long> orgUnitIds = List.of(1L, 999L, 3L);
        Long targetRoomId = 10L;

        when(orgUnitService.assignOrgUnitsToRoom(orgUnitIds, targetRoomId))
                .thenThrow(new ResourceNotFoundException(ResourceType.ORGANIZATIONAL_UNIT, 999L));

        // Act & Assert: Perform PUT request and verify status 404 Not Found
        mockMvc.perform(put("/rooms/{targetRoomId}/org-units", targetRoomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitIds)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("ORGANIZATIONAL_UNIT with ID 999 not found."));
    }

    // TODO allow partial success
    @Test
    void assignOrgUnitsToRoom_OrgUnitInDifferentProject_ShouldReturnBadRequest()
            throws Exception {
        // Arrange: Set up a item IDs and target OrgUnit ID
        List<Long> orgUnitIds = List.of(1L, 2L, 3L);
        Long targetRoomId = 10L;

        when(orgUnitService.assignOrgUnitsToRoom(orgUnitIds, targetRoomId))
                .thenThrow(new IllegalArgumentException(
                        OrgUnitService.PROJECT_MISMATCH_ERROR));

        // Act & Assert: Perform PUT request and verify status 400 Bad Request
        mockMvc.perform(put("/rooms/{targetRoomId}/org-units", targetRoomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orgUnitIds)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(
                                OrgUnitService.PROJECT_MISMATCH_ERROR));
    }

    @Test
    void deleteOneRoom_ShouldDeleteRoom_WhenRoomExists() throws Exception {
        // Act: Perform a DELETE request to the /rooms/1 endpoint
        mockMvc.perform(delete("/rooms/1"))
                .andExpect(status().isNoContent());

        // Assert: Ensure the service method was called to delete the room by ID
        verify(roomService).deleteRoom(1L);
    }

    @Test
    void deleteOneRoom_ShouldReturnNotFound_WhenRoomDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw RoomNotFoundException when deleting a
        // non-existent room
        doThrow(new ResourceNotFoundException(ResourceType.ROOM, 1L)).when(roomService).deleteRoom(1L);

        // Act: Perform a DELETE request to the /rooms/1 endpoint
        mockMvc.perform(delete("/rooms/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("ROOM with ID 1 not found."));

        // Assert: Ensure the service method was called to attempt to delete the room
        verify(roomService).deleteRoom(1L);
    }

    @Test
    void getRoomOrgUnits_ShouldReturnOrgUnits_WhenRoomExists() throws Exception {
        // Arrange: Set up a room with a orgUnit and mock the service to return the
        // room
        Room room = new Room("Test Room", "Room Description", mockProject);
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().room(room).build();
        room.setOrgUnits(Collections.singletonList(orgUnit));
        when(roomService.getRoomById(1L)).thenReturn(room);

        // Act: Perform a GET request to the /rooms/1/org-units endpoint
        mockMvc.perform(get("/rooms/1/org-units"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected orgUnit name
                .andExpect(jsonPath("$[0].name").value(orgUnit.getName()));

        // Assert: Ensure the service method was called to retrieve the room by ID
        verify(roomService).getRoomById(1L);
    }

    @Test
    void getRoomOrgUnits_ShouldReturnNotFound_WhenRoomDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw RoomNotFoundException when retrieving
        // orgUnits for a non-existent room
        when(roomService.getRoomById(1L)).thenThrow(new ResourceNotFoundException(ResourceType.ROOM, 1L));

        // Act: Perform a GET request to the /rooms/1/org-units endpoint
        mockMvc.perform(get("/rooms/1/org-units"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("ROOM with ID 1 not found."));

        // Assert: Ensure the service method was called to attempt to retrieve the
        // room
        verify(roomService).getRoomById(1L);
    }
}
