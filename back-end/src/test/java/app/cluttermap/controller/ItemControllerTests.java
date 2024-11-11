package app.cluttermap.controller;

import static org.hamcrest.Matchers.contains;
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

import app.cluttermap.exception.item.ItemNotFoundException;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewItemDTO;
import app.cluttermap.model.dto.UpdateItemDTO;
import app.cluttermap.service.ItemService;
import app.cluttermap.service.RoomService;
import app.cluttermap.service.SecurityService;

@WebMvcTest(ItemController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class ItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private SecurityService securityService;

    private User mockUser;
    private Project mockProject;
    private Room mockRoom;
    private OrgUnit mockOrgUnit;

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
        mockProject = new Project("Mock Project", mockUser);
        mockRoom = new Room("Mock Room", "Room Description", mockProject);
        mockOrgUnit = new OrgUnit("Mock Org Unit", "Org Unit Description", mockRoom);

        when(securityService.getCurrentUser()).thenReturn(mockUser);
        // Stub isResourceOwner to allow access to protected resources
        when(securityService.isResourceOwner(anyLong(), eq("orgUnit"))).thenReturn(true);
        when(securityService.isResourceOwner(anyLong(), eq("item"))).thenReturn(true);
        when(securityService.isResourceOwner(anyLong(), eq("project"))).thenReturn(true);
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getUserItems_ShouldReturnAllUserItems() throws Exception {
        // Arrange: Set up mock user items and mock the service to return them
        Item item1 = new Item("Test Item 1", "Description 1", List.of("Tag 11", "Tag 12"), mockOrgUnit);
        Item item2 = new Item("Test Item 2", "Description 2", List.of("Tag 21", "Tag 22"), mockOrgUnit);
        when(itemService.getUserItems()).thenReturn(List.of(item1, item2));

        // Act: Perform a GET request to the /items endpoint
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected item names
                .andExpect(jsonPath("$[0].name").value("Test Item 1"))
                .andExpect(jsonPath("$[0].description").value("Description 1"))
                .andExpect(jsonPath("$[0].tags").value(contains("Tag 11", "Tag 12")))
                .andExpect(jsonPath("$[1].name").value("Test Item 2"))
                .andExpect(jsonPath("$[1].description").value("Description 2"))
                .andExpect(jsonPath("$[1].tags").value(contains("Tag 21", "Tag 22")));

        // Assert: Ensure that the service method was called
        verify(itemService).getUserItems();
    }

    @Test
    void getUserItems_ShouldReturnEmptyList_WhenNoItemsExist() throws Exception {
        // Arrange: Set up the service to return an empty list
        when(itemService.getUserItems()).thenReturn(Collections.emptyList());

        // Act: Perform a GET request to the /items endpoint
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains an empty array
                .andExpect(jsonPath("$").isEmpty());

        // Assert: Ensure that the service method was called
        verify(itemService).getUserItems();
    }

    @Test
    void getOneItem_ShouldReturnItem_WhenItemExists() throws Exception {
        // Arrange: Set up a mock item and stub the service to return it when
        // searched by ID
        Item item = new Item("Test Item", "Item description", List.of("tag 1"), mockOrgUnit);
        when(itemService.getItemById(1L)).thenReturn(item);

        // Act: Perform a GET request to the /items/1 endpoint
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                // Assert: Verify the response contains the expected item name
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Item description"))
                .andExpect(jsonPath("$.tags").value(contains("tag 1")));

        // Assert: Ensure that the service method was called
        verify(itemService).getItemById(1L);
    }

    @Test
    void getOneItem_ShouldReturnNotFound_WhenItemDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw ItemNotFoundException when a
        // non-existent item ID is requested
        when(itemService.getItemById(1L)).thenThrow(new ItemNotFoundException());

        // Act: Perform a GET request to the /items/1 endpoint
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isNotFound());

        // Assert: Ensure that the service method was called
        verify(itemService).getItemById(1L);
    }

    @Test
    void addOneItem_ShouldCreateItem_WhenValidRequest() throws Exception {
        // Arrange: Set up a NewItemDTO with valid data and mock the service to
        // return a new item
        NewItemDTO itemDTO = new NewItemDTO("New Item", "Item Description", List.of("tag 1"), String.valueOf(1L));
        Item newItem = new Item(itemDTO.getName(), itemDTO.getDescription(), itemDTO.getTags(), mockOrgUnit);
        when(itemService.createItem(any(NewItemDTO.class))).thenReturn(newItem);

        // Act: Perform a POST request to the /items endpoint with the item data
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected item name
                .andExpect(jsonPath("$.name").value("New Item"));

        // Assert: Ensure the service method was called to create the item
        verify(itemService).createItem(any(NewItemDTO.class));
    }

    @Test
    void addOneItem_ShouldReturnBadRequest_WhenItemNameIsBlank() throws Exception {
        // Arrange: Set up a NewItemDTO with a blank name to trigger validation
        NewItemDTO itemDTO = new NewItemDTO("", "Description", List.of("tag 1"), String.valueOf(1L));

        // Act: Perform a POST request to the /items endpoint with the blank item
        // name
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Item name must not be blank."));
    }

    @Test
    void addOneItem_ShouldReturnBadRequest_WhenOrgUnitNameIsNull() throws Exception {
        // Arrange: Set up a NewItemDTO with a null name to trigger validation
        NewItemDTO itemDTO = new NewItemDTO(null, "Description", List.of("tag 1"), String.valueOf(1L));

        // Act: Perform a POST request to the /items endpoint with the null item
        // name
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Item name must not be blank."));
    }

    @Test
    void addOneItem_ShouldReturnBadRequest_WhenOrgUnitIdIsNull() throws Exception {
        // Arrange: Set up a NewItemDTO with a null item ID to trigger validation
        NewItemDTO itemDTO = new NewItemDTO("Item Name", "Description", List.of("tag 1"), null);

        // Act: Perform a POST request to the /items endpoint with the null item
        // ID
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("orgUnitId"))
                .andExpect(jsonPath("$.errors[0].message").value("OrgUnit ID must not be blank."));
    }

    @Test
    void addOneItem_ShouldReturnBadRequest_WhenOrgUnitIdIsNaN() throws Exception {
        // Arrange: Set up a NewItemDTO with a NaN item ID to trigger validation
        NewItemDTO itemDTO = new NewItemDTO("Item Name", "Description", List.of("tag 1"), "string");

        // Act: Perform a POST request to the /items endpoint with the NaN item
        // ID
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("orgUnitId"))
                .andExpect(jsonPath("$.errors[0].message").value("OrgUnit ID must be a valid number."));
    }

    @Test
    void updateOneItem_ShouldUpdateItem_WhenValidRequest() throws Exception {
        // Arrange: Set up an UpdateItemDTO with a new name and mock the service to
        // return the updated item
        UpdateItemDTO itemDTO = new UpdateItemDTO("Updated Item", "Updated Description", List.of("Updated Tag"));
        Item updatedItem = new Item(itemDTO.getName(), itemDTO.getDescription(), itemDTO.getTags(), mockOrgUnit);
        when(itemService.updateItem(eq(1L), any(UpdateItemDTO.class))).thenReturn(updatedItem);

        // Act: Perform a PUT request to the /items/1 endpoint with the update data
        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the updated item name
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.tags").value(contains("Updated Tag")));
        // .andExpect(jsonPath("$.tags", contains("Updated Tag")));

        // Assert: Ensure the service method was called
        verify(itemService).updateItem(eq(1L), any(UpdateItemDTO.class));
    }

    @Test
    void updateOneItem_ShouldReturnBadRequest_WhenItemNameIsBlank() throws Exception {
        // Arrange: Set up an UpdateItemDTO with a blank item name to trigger
        // validation
        UpdateItemDTO itemDTO = new UpdateItemDTO("", "Updated Description", List.of("Updated Tag"));

        // Act: Perform a PUT request to the /items/1 endpoint with the invalid
        // item name
        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Item name must not be blank."));
    }

    @Test
    void updateOneItem_ShouldReturnBadRequest_WhenItemNameIsNull() throws Exception {
        // Arrange: Set up an UpdateItemDTO with a null item name to trigger
        // validation
        UpdateItemDTO itemDTO = new UpdateItemDTO(null, "Updated Description", List.of("Updated Tag"));

        // Act: Perform a PUT request to the /items/1 endpoint with the null item
        // name
        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Item name must not be blank."));
    }

    @Test
    void moveItemBetweenOrgUnits_Success() throws Exception {
        // Arrange: Set up mock item and service behavior to simulate a successful move.
        Long itemId = 1L;
        Long targetOrgUnitId = 2L;
        Item item = new Item("Test Item", "Item Description", List.of("tag1"), mockOrgUnit);

        when(itemService.moveItemBetweenOrgUnits(itemId, targetOrgUnitId)).thenReturn(item);

        // Act & Assert: Perform the PUT request and verify that it returns status 200
        // OK and correct item data.
        mockMvc.perform(put("/items/{itemId}/move-org-unit/{orgUnitId}", itemId, targetOrgUnitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void moveItemBetweenOrgUnits_OrgUnitNotInSameProject_ShouldReturnBadRequest() throws Exception {
        // Arrange: Set up item ID, target OrgUnit ID, and simulate service throwing
        // IllegalArgumentException.
        Long itemId = 1L;
        Long targetOrgUnitId = 2L;

        when(itemService.moveItemBetweenOrgUnits(itemId, targetOrgUnitId))
                .thenThrow(new IllegalArgumentException("Cannot move item to a different project's OrgUnit"));

        // Act & Assert: Perform the PUT request and verify status 400 Bad Request and
        // error message.
        mockMvc.perform(put("/items/{itemId}/move-org-unit/{orgUnitId}", itemId, targetOrgUnitId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Cannot move item to a different project's OrgUnit"));
    }

    @Test
    void moveItemBetweenOrgUnits_ItemNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange: Set up item ID, target OrgUnit ID, and simulate service throwing
        // ItemNotFoundException.
        Long itemId = 1L;
        Long targetOrgUnitId = 2L;

        when(itemService.moveItemBetweenOrgUnits(itemId, targetOrgUnitId))
                .thenThrow(new ItemNotFoundException());

        // Act & Assert: Perform the PUT request and verify status 404 Not Found.
        mockMvc.perform(put("/items/{itemId}/move-org-unit/{orgUnitId}", itemId, targetOrgUnitId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOneItem_ShouldDeleteItem_WhenItemExists() throws Exception {
        // Act: Perform a DELETE request to the /items/1 endpoint
        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isNoContent());

        // Assert: Ensure the service method was called to delete the item by ID
        verify(itemService).deleteItem(1L);
    }

    @Test
    void deleteOneItem_ShouldReturnNotFound_WhenItemDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw ItemNotFoundException when deleting a
        // non-existent item
        doThrow(new ItemNotFoundException()).when(itemService).deleteItem(1L);

        // Act: Perform a DELETE request to the /items/1 endpoint
        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isNotFound());

        // Assert: Ensure the service method was called to attempt to delete the item
        verify(itemService).deleteItem(1L);
    }
}
