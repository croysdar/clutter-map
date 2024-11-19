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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.cluttermap.TestDataFactory;
import app.cluttermap.exception.ResourceNotFoundException;
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
import app.cluttermap.util.ResourceType;

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

    private Project mockProject;
    private OrgUnit mockOrgUnit;

    @BeforeEach
    void setUp() {
        User mockUser = new User("mockProviderId");
        mockProject = new Project("Mock Project", mockUser);
        mockOrgUnit = new TestDataFactory.OrgUnitBuilder().room(
                new TestDataFactory.RoomBuilder().project(mockProject).build()).build();

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
        List<String> names = List.of("Test Item 1", "Test Item 2");
        Item item1 = new TestDataFactory.ItemBuilder().project(mockProject).name(names.get(0)).build();
        Item item2 = new TestDataFactory.ItemBuilder().project(mockProject).name(names.get(1)).build();

        when(itemService.getUserItems()).thenReturn(List.of(item1, item2));

        // Act: Perform a GET request to the /items endpoint
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains 2 items
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

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
        Item item = new TestDataFactory.ItemBuilder().project(mockProject).build();
        when(itemService.getItemById(1L)).thenReturn(item);

        // Act: Perform a GET request to the /items/1 endpoint
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                // Assert: Verify the response contains the expected item name
                .andExpect(jsonPath("$.name").value(item.getName()))
                .andExpect(jsonPath("$.description").value(item.getDescription()))
                .andExpect(jsonPath("$.tags").value(contains(item.getTags().toArray())))
                .andExpect(jsonPath("$.quantity").value(item.getQuantity()));

        // Assert: Ensure that the service method was called
        verify(itemService).getItemById(1L);
    }

    @Test
    void getOneItem_ShouldReturnNotFound_WhenItemDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw ResourceNotFoundException when a
        // non-existent item ID is requested
        when(itemService.getItemById(1L)).thenThrow(new ResourceNotFoundException(ResourceType.ITEM, 1L));

        // Act: Perform a GET request to the /items/1 endpoint
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("ITEM with ID 1 not found."));

        // Assert: Ensure that the service method was called
        verify(itemService).getItemById(1L);
    }

    @Test
    void addOneItem_ShouldCreateItem_WhenValidRequest() throws Exception {
        // Arrange: Set up a NewItemDTO with valid data and mock the service to
        // return a new item
        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder().build();
        Item newItem = new TestDataFactory.ItemBuilder().fromDTO(itemDTO).orgUnit(mockOrgUnit).build();
        when(itemService.createItem(any(NewItemDTO.class))).thenReturn(newItem);

        // Act: Perform a POST request to the /items endpoint with the item data
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected item name
                .andExpect(jsonPath("$.name").value(itemDTO.getName()));

        // Assert: Ensure the service method was called to create the item
        verify(itemService).createItem(any(NewItemDTO.class));
    }

    @Test
    void addOneItem_ShouldReturnBadRequest_WhenItemNameIsBlank() throws Exception {
        // Arrange: Set up a NewItemDTO with a blank name to trigger validation
        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder().name("").build();

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
    void addOneItem_ShouldReturnBadRequest_WhenItemNameIsNull() throws Exception {
        // Arrange: Set up a NewItemDTO with a null name to trigger validation
        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder().name(null).build();

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
    void addOneItem_ShouldReturnBadRequest_WhenOrgUnitIdAndProjectIsNull() throws Exception {
        // Arrange: Set up a NewItemDTO with a null item ID to trigger validation
        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder().projectId((String) null)
                .orgUnitId((String) null)
                .build();

        // Act: Perform a POST request to the /items endpoint with the null org unit
        // ID
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("orgUnitOrProjectValid"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("Either OrgUnitId or ProjectId must be provided."));
    }

    @Test
    void addOneItem_ShouldReturnBadRequest_WhenOrgUnitIdIsNaN() throws Exception {
        // Arrange: Set up a NewItemDTO with a NaN item ID to trigger validation
        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder().orgUnitId("invalid").build();

        // Act: Perform a POST request to the /items endpoint with the NaN org unit
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
    void addOneItem_ShouldReturnBadRequest_WhenQuantityIsLessThan1() throws Exception {
        // Arrange: Set up a NewItemDTO with a 0 quantity to trigger validation
        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder().quantity(0).build();

        // Act: Perform a POST request to the /items endpoint with the 0 quantity
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the quantity field
                .andExpect(jsonPath("$.errors[0].field").value("quantity"))
                .andExpect(jsonPath("$.errors[0].message").value("Quantity must be a least 1."));
    }

    @Test
    void addOneItem_ShouldSetQuantity1_WhenQuantityNull() throws Exception {
        // Arrange: Set up a NewItemDTO with a null quantity
        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder().quantity(null).build();
        Item newItem = new TestDataFactory.ItemBuilder().fromDTO(itemDTO).orgUnit(mockOrgUnit).build();

        when(itemService.createItem(any(NewItemDTO.class))).thenReturn(newItem);

        // Act: Perform a POST request to the /items endpoint with the null quantity
        mockMvc.perform(post("/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))

                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected item quantity
                .andExpect(jsonPath("$.quantity").value(1));

        // Assert: Ensure the service method was called to create the item
        verify(itemService).createItem(any(NewItemDTO.class));
    }

    @Test
    void updateOneItem_ShouldUpdateItem_WhenValidRequest() throws Exception {
        // Arrange: Set up an UpdateItemDTO with a new name and mock the service to
        // return the updated item
        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder().build();
        Item updatedItem = new TestDataFactory.ItemBuilder().fromDTO(itemDTO).orgUnit(mockOrgUnit).build();

        when(itemService.updateItem(eq(1L), any(UpdateItemDTO.class))).thenReturn(updatedItem);

        // Act: Perform a PUT request to the /items/1 endpoint with the update data
        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the updated item name
                .andExpect(jsonPath("$.name").value(itemDTO.getName()))
                .andExpect(jsonPath("$.description").value(itemDTO.getDescription()))
                .andExpect(jsonPath("$.tags").value(contains(itemDTO.getTags().toArray())))
                .andExpect(jsonPath("$.quantity").value(itemDTO.getQuantity()));

        // Assert: Ensure the service method was called
        verify(itemService).updateItem(eq(1L), any(UpdateItemDTO.class));
    }

    @Test
    void updateOneItem_ShouldReturnBadRequest_WhenItemNameIsBlank() throws Exception {
        // Arrange: Set up an UpdateItemDTO with a blank item name to trigger
        // validation
        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder().name("").build();

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
        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder().name(null).build();

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
    void updateOneItem_ShouldReturnBadRequest_WhenQuantityIsLessThan1() throws Exception {
        // Arrange: Set up an UpdateItemDTO with a 0 quantity
        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder().quantity(0).build();

        // Act: Perform a POST request to the /items endpoint with the 0 quantity
        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the quantity field
                .andExpect(jsonPath("$.errors[0].field").value("quantity"))
                .andExpect(jsonPath("$.errors[0].message").value("Quantity must be a least 1."));
    }

    @Test
    void updateOneItem_ShouldSetQuantity1_WhenQuantityNull() throws Exception {
        // Arrange: Set up a UpdateItemDTO with a null quantity
        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder().quantity(null).build();

        Item updatedItem = new TestDataFactory.ItemBuilder().fromDTO(itemDTO).orgUnit(mockOrgUnit).build();

        when(itemService.updateItem(eq(1L), any(UpdateItemDTO.class))).thenReturn(updatedItem);

        // Act: Perform a PUT request to the /items endpoint with the null quantity
        mockMvc.perform(put("/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDTO)))

                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected item quantity
                .andExpect(jsonPath("$.quantity").value(1));

        // Assert: Ensure the service method was called to update the item
        verify(itemService).updateItem(eq(1L), any(UpdateItemDTO.class));
    }

    @Test
    void unassignItems_Success() throws Exception {
        // Arrange: Set up itemIds, and simulate a successful unassign
        List<Long> itemIds = List.of(1L, 2L, 3L);
        List<String> itemNames = List.of("Item 1", "Item 2", "Item 3");
        List<Item> unassignedItems = List.of(
                new TestDataFactory.ItemBuilder().name(itemNames.get(0)).orgUnit(mockOrgUnit).build(),
                new TestDataFactory.ItemBuilder().name(itemNames.get(1)).orgUnit(mockOrgUnit).build(),
                new TestDataFactory.ItemBuilder().name(itemNames.get(2)).orgUnit(mockOrgUnit).build());

        when(itemService.unassignItems(itemIds)).thenReturn(unassignedItems);

        // Act & Assert: Perform the PUT request and verify status 200 OK and correct
        // item data
        mockMvc.perform(put("/items/unassign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(itemNames.get(0)))
                .andExpect(jsonPath("$[1].name").value(itemNames.get(1)))
                .andExpect(jsonPath("$[2].name").value(itemNames.get(2)));

        // Assert: Ensure the service method was called to unassign the items
        verify(itemService).unassignItems(itemIds);
    }

    // TODO: Allow partial success
    @Test
    void unassignItems_ItemNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange: Set up item IDs, including a non-existent item ID
        List<Long> itemIds = List.of(1L, 999L, 3L);

        // Simulate ResourceNotFoundException for one of the items
        when(itemService.unassignItems(itemIds))
                .thenThrow(new ResourceNotFoundException(ResourceType.ITEM, 999L));

        // Act & Assert: Perform the PUT request and verify status 404 Not Found with an
        // error message
        mockMvc.perform(put("/items/unassign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemIds)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("ITEM with ID 999 not found."));
    }

    @Test
    void unassignItems_UserDoesNotOwnItem_ShouldThrowAccessDenied() throws Exception {
        // Arrange: Set up itemIds
        List<Long> itemIds = List.of(1L, 2L, 3L);

        String message = String.format(ItemService.ACCESS_DENIED_STRING, 1L);

        doThrow(new AccessDeniedException(message)).when(itemService).checkOwnershipForItems(itemIds);

        // Act & Assert: Expect an AccessDeniedException when attempting to unassign
        // items
        mockMvc.perform(put("/items/unassign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemIds)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Access Denied"));
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
        // Arrange: Mock the service to throw ResourceNotFoundException when deleting a
        // non-existent item
        doThrow(new ResourceNotFoundException(ResourceType.ITEM, 1L)).when(itemService).deleteItem(1L);

        // Act: Perform a DELETE request to the /items/1 endpoint
        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("ITEM with ID 1 not found."));

        // Assert: Ensure the service method was called to attempt to delete the item
        verify(itemService).deleteItem(1L);
    }

}
