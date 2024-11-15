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

import app.cluttermap.exception.room.RoomNotFoundException;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewRoomDTO;
import app.cluttermap.model.dto.UpdateRoomDTO;
import app.cluttermap.service.RoomService;
import app.cluttermap.service.SecurityService;

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
        when(roomService.getRoomById(1L)).thenThrow(new RoomNotFoundException());

        // Act: Perform a GET request to the /rooms/1 endpoint
        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isNotFound());

        // Assert: Ensure that the service method was called
        verify(roomService).getRoomById(1L);
    }

    @Test
    void addOneRoom_ShouldCreateRoom_WhenValidRequest() throws Exception {
        // Arrange: Set up a NewRoomDTO with valid data and mock the service to
        // return a new room
        NewRoomDTO roomDTO = new NewRoomDTO("New Room", "Room Description", String.valueOf(1L));
        Room newRoom = new Room(roomDTO.getName(), roomDTO.getDescription(), mockProject);
        when(roomService.createRoom(any(NewRoomDTO.class))).thenReturn(newRoom);

        // Act: Perform a POST request to the /rooms endpoint with the room data
        mockMvc.perform(post("/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected room name
                .andExpect(jsonPath("$.name").value("New Room"));

        // Assert: Ensure the service method was called to create the room
        verify(roomService).createRoom(any(NewRoomDTO.class));
    }

    @Test
    void addOneRoom_ShouldReturnBadRequest_WhenRoomNameIsBlank() throws Exception {
        // Arrange: Set up a NewRoomDTO with a blank name to trigger validation
        NewRoomDTO roomDTO = new NewRoomDTO("", "", String.valueOf(1L));

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
        NewRoomDTO roomDTO = new NewRoomDTO(null, "", String.valueOf(1L));

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
        NewRoomDTO roomDTO = new NewRoomDTO("Project Name", "", null);

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
        NewRoomDTO roomDTO = new NewRoomDTO("Project Name", "", "string");

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
        UpdateRoomDTO roomDTO = new UpdateRoomDTO("Updated Room", "Updated Description");
        Room updatedRoom = new Room(roomDTO.getName(), roomDTO.getDescription(), mockProject);
        when(roomService.updateRoom(eq(1L), any(UpdateRoomDTO.class))).thenReturn(updatedRoom);

        // Act: Perform a PUT request to the /rooms/1 endpoint with the update data
        mockMvc.perform(put("/rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the updated room name
                .andExpect(jsonPath("$.name").value("Updated Room"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        // Assert: Ensure the service method was called
        verify(roomService).updateRoom(eq(1L), any(UpdateRoomDTO.class));
    }

    @Test
    void updateOneRoom_ShouldReturnBadRequest_WhenRoomNameIsBlank() throws Exception {
        // Arrange: Set up an UpdateRoomDTO with a blank room name to trigger
        // validation
        UpdateRoomDTO roomDTO = new UpdateRoomDTO("", "Updated Description");

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
        UpdateRoomDTO roomDTO = new UpdateRoomDTO(null, "Updated Description");

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
        doThrow(new RoomNotFoundException()).when(roomService).deleteRoom(1L);

        // Act: Perform a DELETE request to the /rooms/1 endpoint
        mockMvc.perform(delete("/rooms/1"))
                .andExpect(status().isNotFound());

        // Assert: Ensure the service method was called to attempt to delete the room
        verify(roomService).deleteRoom(1L);
    }

    @Test
    void getRoomOrgUnits_ShouldReturnOrgUnits_WhenRoomExists() throws Exception {
        // Arrange: Set up a room with a orgUnit and mock the service to return the
        // room
        Room room = new Room("Test Room", "Room Description", mockProject);
        OrgUnit orgUnit = new OrgUnit("White Shelving Unit", "This is a shelving unit.", room);
        room.setOrgUnits(Collections.singletonList(orgUnit));
        when(roomService.getRoomById(1L)).thenReturn(room);

        // Act: Perform a GET request to the /rooms/1/org-units endpoint
        mockMvc.perform(get("/rooms/1/org-units"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected orgUnit name
                .andExpect(jsonPath("$[0].name").value("White Shelving Unit"))
                .andExpect(jsonPath("$[0].description").value("This is a shelving unit."));

        // Assert: Ensure the service method was called to retrieve the room by ID
        verify(roomService).getRoomById(1L);
    }

    @Test
    void getRoomOrgUnits_ShouldReturnNotFound_WhenRoomDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw RoomNotFoundException when retrieving
        // orgUnits for a non-existent room
        when(roomService.getRoomById(1L)).thenThrow(new RoomNotFoundException());

        // Act: Perform a GET request to the /rooms/1/org-units endpoint
        mockMvc.perform(get("/rooms/1/org-units"))
                .andExpect(status().isNotFound());

        // Assert: Ensure the service method was called to attempt to retrieve the
        // room
        verify(roomService).getRoomById(1L);
    }
}
