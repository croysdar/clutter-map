package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import app.cluttermap.TestDataFactory;
import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.exception.item.ItemLimitReachedException;
import app.cluttermap.model.Event;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewItemDTO;
import app.cluttermap.model.dto.UpdateItemDTO;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.ProjectRepository;
import app.cluttermap.repository.RoomRepository;
import app.cluttermap.util.ResourceType;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ItemServiceTests {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrgUnitRepository orgUnitRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private ProjectService projectService;

    @Mock
    private RoomService roomService;

    @Mock
    private OrgUnitService orgUnitService;

    @Mock
    private EventService eventService;

    @InjectMocks
    private ItemService itemService;

    private User mockUser;
    private Project mockProject;
    private Room mockRoom;
    private OrgUnit mockOrgUnit;

    private static int ITEM_LIMIT = 1000;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(itemService, "self", itemService);

        mockUser = new User("mockProviderId");

        mockProject = new TestDataFactory.ProjectBuilder().user(mockUser).build();

        mockRoom = new TestDataFactory.RoomBuilder().project(mockProject).build();

        mockOrgUnit = new TestDataFactory.OrgUnitBuilder().room(mockRoom).build();
    }

    @Test
    void getUserItems_ShouldReturnItemsOwnedByUser() {
        // Arrange: Mock the current user and items
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        Item item1 = new TestDataFactory.ItemBuilder().orgUnit(mockOrgUnit).build();
        Item item2 = new TestDataFactory.ItemBuilder().project(mockProject).build();
        when(itemRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(item1, item2));

        // Act: Call service method
        Iterable<Item> userItems = itemService.getUserItems();

        // Assert: Verify the result contains the expected items
        assertThat(userItems).containsExactly(item1, item2)
                .as("Items owned by user should be returned when they exist");

        // Verify dependencies are called as expected
        verify(securityService).getCurrentUser();
        verify(itemRepository).findByOwnerId(mockUser.getId());
    }

    @Test
    void getUserItems_ShouldReturnEmptyList_WhenNoItemsExist() {
        // Arrange: Mock the current user and an empty repository result
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(itemRepository.findByOwnerId(mockUser.getId())).thenReturn(Collections.emptyList());

        // Act: Call service method
        Iterable<Item> userItems = itemService.getUserItems();

        // Assert: Verify the result is empty
        assertThat(userItems)
                .as("Empty list should be returned when user owns no items")
                .isEmpty();

        // Verify dependencies are called as expected
        verify(securityService).getCurrentUser();
        verify(itemRepository).findByOwnerId(mockUser.getId());
    }

    @ParameterizedTest
    @CsvSource({
            "true, Item should be returned when it exists",
            "false, ResourceNotFoundException should be thrown when item does not exist"
    })
    void getItemById_ShouldHandleExistenceCorrectly(boolean itemExists, String description) {
        // Arrange
        Long resourceId = 1L;
        if (itemExists) {
            // Arrange: Mock the repository to return an item
            Item mockItem = mockAssignedItemInRepository(resourceId);

            // Act: Call service method
            Item foundItem = itemService.getItemById(resourceId);

            // Assert: Verify the item retrieved matches the mock
            assertThat(foundItem)
                    .as(description)
                    .isNotNull()
                    .isEqualTo(mockItem);

        } else {
            // Arrange: Mock the repository to return empty
            mockNonexistentItemInRepository(resourceId);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> itemService.getItemById(resourceId),
                    description);
        }

        // Verify: Ensure repository interaction occurred
        verify(itemRepository).findById(anyLong());
    }

    @ParameterizedTest
    @CsvSource({
            "true, Unassigned item should be returned when the project has unassigned items",
            "false, Empty list should be returned when the project has no unassigned items"
    })
    void getUnassignedItemsByProjectId_ShouldReturnCorrectItems(boolean unassignedItemsExist,
            String description) {
        // Arrange: Prepare mock data
        List<Item> mockItems = unassignedItemsExist
                ? List.of(new TestDataFactory.ItemBuilder().project(mockProject).build())
                : List.of();

        Long projectId = mockProject.getId();
        when(itemRepository.findUnassignedItemsByProjectId(projectId)).thenReturn(mockItems);

        // Act: Call the service method
        List<Item> unassignedItems = itemService.getUnassignedItemsByProjectId(projectId);

        // Assert: Verify the result
        assertThat(unassignedItems)
                .as(description)
                .isEqualTo(mockItems);

        // Verify: Ensure repository interaction occurred
        verify(itemRepository).findUnassignedItemsByProjectId(projectId);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Item should be created in the org unit when orgUnitId is provided",
            "false, Item should be created as unassigned when projectId is provided and orgUnitId is null"
    })
    void createItem_ShouldCreateItem(boolean isOrgUnitProvided, String description) {
        Long orgUnitId = isOrgUnitProvided ? mockOrgUnit.getId() : null;
        Long projectId = isOrgUnitProvided ? null : mockProject.getId();

        // Arrange: Prepare the Item DTO based on the parameters
        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder()
                .orgUnitId(orgUnitId)
                .projectId(projectId)
                .build();

        Item mockItem;
        if (isOrgUnitProvided) {
            // Mock org unit and corresponding item
            mockOrgUnitLookup();
            mockItem = new TestDataFactory.ItemBuilder().fromDTO(itemDTO).orgUnit(mockOrgUnit).build();
        } else {
            // Mock project and corresponding item
            mockProjectLookup();
            mockItem = new TestDataFactory.ItemBuilder().fromDTO(itemDTO).project(mockProject).build();
        }

        when(itemRepository.save(any(Item.class))).thenReturn(mockItem);

        // Arrange: Mock event logging
        mockLogCreateEvent();

        // Act: Call the service method
        Item createdItem = itemService.createItem(itemDTO);

        // Assert: Validate the created item
        assertThat(createdItem)
                .as(description)
                .isNotNull()
                .isEqualTo(mockItem);

        // Verify that the correct service and repository methods were called
        if (isOrgUnitProvided) {
            verify(orgUnitService).getOrgUnitById(orgUnitId);
        } else {
            verify(projectService).getProjectById(projectId);
        }
        verify(itemRepository).save(any(Item.class));

        // Capture and verify the arguments passed to logUpdateEvent
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        // Assert: Verify event logging
        verify(eventService).logCreateEvent(eq(ResourceType.ITEM), eq(mockItem.getId()), payloadCaptor.capture());

        // Assert: Verify the payload contains the expected values
        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload)
                .containsEntry("name", createdItem.getName());
        assertThat(capturedPayload)
                .containsEntry("description", createdItem.getDescription());
        assertThat(capturedPayload)
                .containsEntry("tags", createdItem.getTags());
        assertThat(capturedPayload)
                .containsEntry("quantity", createdItem.getQuantity());
        if (isOrgUnitProvided) {
            assertThat(capturedPayload)
                    .containsEntry("orgUnitId", createdItem.getOrgUnitId());
        } else {
            assertThat(capturedPayload)
                    .doesNotContainEntry("orgUnitId", createdItem.getOrgUnitId());
        }
    }

    @Disabled("Feature under development")
    @Test
    void createItem_ShouldThrowException_WhenItemLimitReached() {
        // Arrange: Set up a room with the maximum allowed items
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < ITEM_LIMIT; i++) {
            items.add(new TestDataFactory.ItemBuilder().orgUnit(mockOrgUnit).build());
        }
        mockOrgUnit.setItems(items);

        // Stub the repository to return the room and items
        mockProjectLookup();
        when(itemRepository.findByOwnerId(1L)).thenReturn(items);

        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder().build();

        // Act & Assert: Attempt to create a item and expect an exception
        assertThrows(ItemLimitReachedException.class, () -> itemService.createItem(itemDTO));
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, true, All fields should update when all fields are provided",
            "false, true, true, Description should not update when it is null",
            "true, false, true, Tags should not update when they are null",
            "true, true, false, Quantity should not update when it is null"
    })
    void updateItem_ShouldUpdateFieldsConditionally(
            boolean updateDescription, boolean updateTags, boolean updateQuantity, String description) {
        // Arrange: Set up mock item with initial values
        List<String> oldTags = List.of("Old tag 1", "Old tag 2");
        String oldDescription = "Old Description";
        Integer oldQuantity = 10;

        Item item = new TestDataFactory.ItemBuilder()
                .name("Old Name")
                .description(oldDescription)
                .tags(oldTags)
                .quantity(oldQuantity)
                .orgUnit(mockOrgUnit).build();
        Long resourceId = item.getId();

        when(itemRepository.findById(resourceId)).thenReturn(Optional.of(item));

        // Arrange: Use conditional variables for the expected values of the updated
        // fields
        String newDescription = updateDescription ? "New Description" : null;
        List<String> newTags = updateTags ? List.of("New Tag 1", "New Tag 2") : null;
        Integer newQuantity = updateQuantity ? 5 : null;

        // Build the UpdateItemDTO with these values
        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder()
                .name("New Name")
                .description(newDescription)
                .tags(newTags)
                .quantity(newQuantity)
                .build();

        // Stub the repository to return the item after saving
        when(itemRepository.save(item)).thenReturn(item);

        // Arrange: Mock event logging
        mockLogUpdateEvent();

        // Act: Call the service method
        itemService.updateItem(resourceId, itemDTO);

        // Capture the saved item to verify fields
        ArgumentCaptor<Item> savedItemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(savedItemCaptor.capture());
        Item savedItem = savedItemCaptor.getValue();

        // Assert: Add descriptions for field checks
        assertThat(savedItem.getName())
                .as(description + ": Name should match the expected value")
                .isEqualTo(itemDTO.getName());
        assertThat(savedItem.getDescription())
                .as(description + ": Description should match the expected value")
                .isEqualTo(updateDescription ? newDescription : oldDescription);
        assertThat(savedItem.getTags())
                .as(description + ": Tags should match the expected value")
                .isEqualTo(updateTags ? newTags : oldTags);
        assertThat(savedItem.getQuantity())
                .as(description + ": Quantity should match the expected value")
                .isEqualTo(updateQuantity ? newQuantity : oldQuantity);

        // Capture and verify the arguments passed to logUpdateEvent
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        // Verify the event was logged
        verify(eventService).logUpdateEvent(
                eq(ResourceType.ITEM),
                eq(resourceId),
                payloadCaptor.capture());

        // Assert: Verify the payload contains the expected changes
        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload)
                .containsEntry("name", savedItem.getName());
        if (updateDescription) {
            assertThat(capturedPayload)
                    .containsEntry("description", savedItem.getDescription());
        } else {
            assertThat(capturedPayload)
                    .doesNotContainEntry("description", savedItem.getDescription());
        }
        if (updateTags) {
            assertThat(capturedPayload)
                    .containsEntry("tags", savedItem.getTags());
        } else {
            assertThat(capturedPayload)
                    .doesNotContainEntry("tags", savedItem.getTags());
        }
        if (updateQuantity) {
            assertThat(capturedPayload)
                    .containsEntry("quantity", savedItem.getQuantity());
        } else {
            assertThat(capturedPayload)
                    .doesNotContainEntry("quantity", savedItem.getQuantity());
        }
    }

    @Test
    void updateItem_ShouldThrowException_WhenItemDoesNotExist() {
        // Arrange: Stub the repository to return an empty result for non-existent item
        Long nonExistentItemId = 999L;
        mockNonexistentItemInRepository(nonExistentItemId);

        // Arrange: Set up an UpdateItemDTO
        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder().build();

        // Act & Assert: Expect ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> itemService.updateItem(nonExistentItemId, itemDTO));

        // Verify: Ensure save was not called
        verify(itemRepository, never()).save(any(Item.class));
    }

    @ParameterizedTest
    @CsvSource({
            "true, Item should be deleted when it exists",
            "false, Exception should be thrown when item does not exist"
    })
    void deleteItem_ShouldHandleExistenceCorrectly(boolean itemExists, String description) {
        Long resourceId = 1L;
        if (itemExists) {
            // Arrange: Stub the repository to simulate finding item
            mockAssignedItemInRepository(resourceId);

            // Arrange: Mock event logging
            mockLogDeleteEvent();

            // Act: Call the service method
            itemService.deleteItemById(resourceId);

            // Assert: Verify that the repository's delete method was called with the
            // correct ID
            verify(itemRepository).deleteById(resourceId);

            // Assert: Verify event logging
            verify(eventService).logDeleteEvent(
                    eq(ResourceType.ITEM),
                    eq(resourceId));
        } else {
            // Arrange: Stub the repository to simulate not finding item
            mockNonexistentItemInRepository(resourceId);

            // Act & Assert: Attempt to delete the item and expect a
            // ResourceNotFoundException
            assertThrows(ResourceNotFoundException.class, () -> itemService.deleteItemById(resourceId));

            // Assert: Verify that the repository's delete method was never called
            verify(itemRepository, never()).deleteById(anyLong());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "true, Item should be successfully assigned to the org unit",
            "false, ResourceNotFoundException should be thrown when item does not exist"
    })
    void assignItemsToOrgUnit_ShouldHandleExistenceCorrectly(boolean itemExists, String description) {
        // Arrange: Use the existing mockOrgUnit as the target
        mockOrgUnitLookup();

        // Arrange: Mock items to move
        Long resourceId = 1L;
        if (itemExists) {
            mockUnassignedItemInRepository(resourceId);
            // Act: Call the service method
            Iterable<Item> movedItems = itemService.assignItemsToOrgUnit(List.of(resourceId),
                    mockOrgUnit.getId());

            // Assert: Verify that the item is assigned to the target org unit
            assertThat(movedItems)
                    .as(description)
                    .allMatch(item -> item.getOrgUnit().equals(mockOrgUnit));

            // Verify interactions
            verify(itemRepository).findById(resourceId);
            verify(orgUnitRepository).save(any(OrgUnit.class));
        } else {
            mockNonexistentItemInRepository(resourceId);
            // Assert: Expect ResourceNotFoundException
            assertThrows(ResourceNotFoundException.class,
                    () -> itemService.assignItemsToOrgUnit(List.of(resourceId), mockOrgUnit.getId()),
                    description);

            // Verify no interactions with the repository save method
            verify(itemRepository, never()).save(any(Item.class));
        }
    }

    @Test
    void assignItemsToOrgUnit_ShouldHandleAssignedAndUnassignedItems() {
        // Arrange: Use the existing mockOrgUnit as the target
        mockOrgUnitLookup();

        // Mock unassigned items
        Item unassignedItem1 = mockUnassignedItemInRepository(1L);
        Item unassignedItem2 = mockUnassignedItemInRepository(2L);

        // Mock a previously assigned item
        OrgUnit previousOrgUnit = new TestDataFactory.OrgUnitBuilder().id(10L).project(mockProject).build();
        Item assignedItem = mockAssignedItemInRepository(3L, previousOrgUnit);

        // Act: Assign multiple items
        Iterable<Item> movedItems = itemService.assignItemsToOrgUnit(
                List.of(unassignedItem1.getId(), unassignedItem2.getId(), assignedItem.getId()),
                mockOrgUnit.getId());

        // Assert: Verify all items are assigned to the target org unit
        assertThat(movedItems).allMatch(item -> item.getOrgUnit().equals(mockOrgUnit));

        // Verify unassignment and reassignment for the previously assigned item
        verify(orgUnitRepository).save(previousOrgUnit); // Unassign
        verify(orgUnitRepository, times(3)).save(mockOrgUnit); // Assign all items
        verify(itemRepository, times(3)).findById(anyLong());
    }

    @Test
    void assignItemsToOrgUnit_DifferentProject_ShouldThrowIllegalArgumentException() {
        // Arrange: Use the existing mockOrgUnit as the target
        mockOrgUnitLookup();

        // Mock an item from a different project
        Project differentProject = new TestDataFactory.ProjectBuilder().id(2L).user(mockUser).build();
        Item itemWithDifferentProject = new TestDataFactory.ItemBuilder().project(differentProject).build();
        when(itemRepository.findById(2L)).thenReturn(Optional.of(itemWithDifferentProject));

        // Act & Assert: Expect IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> itemService.assignItemsToOrgUnit(List.of(2L), mockOrgUnit.getId()),
                "Should throw IllegalArgumentException for items from different projects");

        // Verify: Ensure no repository updates occurred
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void unassignItem_Success() {
        // Arrange: Mock items and associate them with the org unit
        Item item1 = mockAssignedItemInRepository(1L);
        Item item2 = mockAssignedItemInRepository(2L);

        // Act: Call the service method
        Iterable<Item> unassignedItems = itemService.unassignItems(List.of(item1.getId(), item2.getId()));

        // Assert: Verify that each item's org unit is null
        unassignedItems.forEach(item -> assertThat(item.getOrgUnit()).isNull());

        // Verify repository interactions
        verify(itemRepository, times(2)).findById(anyLong());
        verify(orgUnitRepository, times(2)).save(any(OrgUnit.class));
    }

    @Test
    void unassignItems_ItemNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange: Set up a non-existent item ID
        Long nonExistentItemId = 999L;
        mockNonexistentItemInRepository(nonExistentItemId);

        // Act & Assert: Expect ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> itemService.unassignItems(List.of(nonExistentItemId)));

        // Verify: Ensure save was never called
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUserDoesNotOwnItem() {
        // Overwrite the default stub for `isResourceOwner` to deny access to the item
        List<Long> itemIds = List.of(1L, 2L, 3L);
        when(securityService.isResourceOwner(anyLong(), eq(ResourceType.ITEM))).thenReturn(false);

        // Act & Assert:
        assertThrows(AccessDeniedException.class, () -> {
            itemService.checkOwnershipForItems(itemIds);
        });
    }

    private void mockNonexistentItemInRepository(Long resourceId) {
        when(itemRepository.findById(resourceId)).thenReturn(Optional.empty());
    }

    private Item mockAssignedItemInRepository(Long resourceId) {
        Item mockItem = new TestDataFactory.ItemBuilder().id(resourceId).orgUnit(mockOrgUnit).build();
        when(itemRepository.findById(resourceId)).thenReturn(Optional.of(mockItem));
        return mockItem;
    }

    private Item mockAssignedItemInRepository(Long resourceId, OrgUnit orgUnit) {
        Item mockItem = new TestDataFactory.ItemBuilder().id(resourceId).orgUnit(orgUnit).build();
        when(itemRepository.findById(resourceId)).thenReturn(Optional.of(mockItem));
        return mockItem;
    }

    private Item mockUnassignedItemInRepository(Long resourceId) {
        Item mockItem = new TestDataFactory.ItemBuilder().id(resourceId).project(mockProject).build();
        when(itemRepository.findById(resourceId)).thenReturn(Optional.of(mockItem));
        return mockItem;
    }

    private void mockOrgUnitLookup() {
        when(orgUnitService.getOrgUnitById(mockOrgUnit.getId())).thenReturn(mockOrgUnit);
    }

    private void mockProjectLookup() {
        when(projectService.getProjectById(mockProject.getId())).thenReturn(mockProject);
    }

    private void mockLogCreateEvent() {
        when(eventService.logCreateEvent(any(), anyLong(), any())).thenReturn(new Event());
    }

    private void mockLogUpdateEvent() {
        when(eventService.logUpdateEvent(any(), anyLong(), any())).thenReturn(new Event());
    }

    private void mockLogDeleteEvent() {
        when(eventService.logDeleteEvent(any(), anyLong())).thenReturn(new Event());
    }
}
