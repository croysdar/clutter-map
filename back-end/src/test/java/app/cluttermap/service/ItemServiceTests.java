package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.TestDataFactory;
import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.exception.item.ItemLimitReachedException;
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

    @InjectMocks
    private ItemService itemService;

    private User mockUser;
    private Project mockProject;
    private OrgUnit mockOrgUnit;

    private static int ITEM_LIMIT = 1000;

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
        mockProject = new Project("Mock Project", mockUser);
        mockOrgUnit = new OrgUnit("Mock Org Unit", "", new Room("Mock Room", "Room Description", mockProject));
        mockOrgUnit.setId(1L);
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUserDoesNotOwnItem() {
        // Overwrite the default stub for `isResourceOwner` to deny access to the item
        List<Long> itemIds = List.of(1L, 2L, 3L);
        when(securityService.isResourceOwner(anyLong(), eq("item"))).thenReturn(false);

        // Act & Assert:
        assertThrows(AccessDeniedException.class, () -> {
            itemService.checkOwnershipForItems(itemIds);
        });
    }

    @Test
    void getItemId_ShouldReturnItem_WhenItemExists() {
        // Arrange: Set up a sample org unit and stub the repository to return it by ID
        Item item = new TestDataFactory.ItemBuilder().orgUnit(mockOrgUnit).build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act: Retrieve the org unit using the service method
        Item foundItem = itemService.getItemById(1L);

        // Assert: Verify that the item retrieved matches the expected item
        assertThat(foundItem).isEqualTo(item);
    }

    @Test
    void getItemById_ShouldThrowException_WhenItemDoesNotExist() {
        // Arrange: Stub the repository to return an empty result for a non-existent
        // item
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Attempt to retrieve the item and expect a
        // ItemNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> itemService.getItemById(1L));
    }

    @Test
    void createItem_ShouldCreateItem_WhenValid() {
        // Arrange: Stub org unit retrieval to return mockOrgUnit when the specified ID
        // is used
        when(orgUnitService.getOrgUnitById(1L)).thenReturn(mockOrgUnit);

        // Arrange: Prepare the Item DTO
        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder().build();

        // Arrange: Create an Item that represents the saved item returned by
        // the repository
        Item item = new TestDataFactory.ItemBuilder().fromDTO(itemDTO).orgUnit(mockOrgUnit).build();
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        // Act: create a item using itemService and pass in the item DTO
        Item createdItem = itemService.createItem(itemDTO);

        // Assert: verify that the created item is not null and matches the expected
        // details from itemDTO
        assertThat(createdItem).isNotNull();
        assertThat(createdItem.getName()).isEqualTo(itemDTO.getName());
        assertThat(createdItem.getDescription()).isEqualTo(itemDTO.getDescription());
        assertThat(createdItem.getOrgUnit()).isEqualTo(mockOrgUnit);

        // Verify that itemRepository.save() was called to persist the new item
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItem_ShouldCreateItem_WhenProjectExistsAndOrgUnitIdNull() {
        // Arrange: Stub project retrieval to return mockProject when the specified ID
        // is used
        when(projectService.getProjectById(1L)).thenReturn(mockProject);

        // Arrange: Prepare the Item DTO with the org unit ID as null
        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder().orgUnitId(null).build();

        // Arrange: Create a mock Item that represents the saved Item returned by
        // the repository
        Item item = new TestDataFactory.ItemBuilder().project(mockProject).build();

        when(itemRepository.save(any(Item.class))).thenReturn(item);

        // Act: create a item using itemService and pass in the item DTO
        Item createdItem = itemService.createItem(itemDTO);

        // Assert: verify that the created item is not null and matches the expected
        // details from itemDTO
        assertThat(createdItem).isNotNull();
        assertThat(createdItem.getName()).isEqualTo(itemDTO.getName());
        assertThat(createdItem.getDescription()).isEqualTo(itemDTO.getDescription());
        assertThat(createdItem.getProject()).isEqualTo(mockProject);

        // Verify that itemRepository.save() was called to persist the new item
        verify(itemRepository).save(any(Item.class));
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
        when(projectService.getProjectById(1L)).thenReturn(mockProject);
        when(itemRepository.findByOwnerId(1L)).thenReturn(items);

        NewItemDTO itemDTO = new TestDataFactory.NewItemDTOBuilder().build();

        // Act & Assert: Attempt to create a item and expect an exception
        assertThrows(ItemLimitReachedException.class, () -> itemService.createItem(itemDTO));
    }

    @Test
    void getUserItems_ShouldReturnItemsOwnedByUser() {
        // Arrange: Set up mock user, projects, and items, and stub the repository to
        // return items owned by the user
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        Item item1 = new TestDataFactory.ItemBuilder().orgUnit(mockOrgUnit).build();
        Item item2 = new TestDataFactory.ItemBuilder().orgUnit(mockOrgUnit).build();

        when(itemRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(item1, item2));

        // Act: Retrieve the items owned by the user
        Iterable<Item> userItems = itemService.getUserItems();

        // Assert: Verify that the result contains only the items owned by the user
        assertThat(userItems).containsExactly(item1, item2);
    }

    @Test
    void getUserItems_ShouldReturnItemsAcrossMultipleProjects() {
        // Arrange: Set up two projects for the same user with items
        Project project1 = new Project("Project 1", mockUser);
        Project project2 = new Project("Project 2", mockUser);

        Room room1 = new Room("Room 1", "Room Description 1", project1);
        Room room2 = new Room("Room 2", "Room Description 2", project2);

        OrgUnit orgUnit1 = new OrgUnit("OrgUnit 1", "OrgUnit Description 1", room1);
        OrgUnit orgUnit2 = new OrgUnit("OrgUnit 2", "OrgUnit Description 2", room2);

        Item item1 = new TestDataFactory.ItemBuilder().orgUnit(orgUnit1).build();
        Item item2 = new TestDataFactory.ItemBuilder().orgUnit(orgUnit2).build();

        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(itemRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(item1, item2));

        // Act: Fetch items for the user
        Iterable<Item> userItems = itemService.getUserItems();

        // Assert: Verify both items are returned across different projects
        assertThat(userItems).containsExactlyInAnyOrder(item1, item2);
    }

    @Test
    void getUserItems_ShouldReturnEmptyList_WhenNoItemsExist() {
        // Arrange: Set up mock user and stub the repository to return an empty list
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(itemRepository.findByOwnerId(mockUser.getId())).thenReturn(Collections.emptyList());

        // Act: Retrieve the items owned by the user
        Iterable<Item> userItems = itemService.getUserItems();

        // Assert: Verify that the result is empty
        assertThat(userItems).isEmpty();
    }

    @Test
    void getUnassignedItemsByProjectId_ShouldReturnOnlyUnassignedItems() {
        // Arrange: Mock repository method
        Long projectId = 1L;
        Item unassignedItem = new TestDataFactory.ItemBuilder().project(mockProject).build();
        when(itemRepository.findUnassignedItemsByProjectId(projectId)).thenReturn(List.of(unassignedItem));

        // Act: Call service method
        List<Item> unassignedItems = itemService.getUnassignedItemsByProjectId(projectId);

        // Assert: Verify correct items are returned
        assertThat(unassignedItems).containsExactly(unassignedItem);
    }

    @Test
    void getUnassignedItemsByNonExistentProjectId_ShouldReturnEmptyList() {
        // Arrange: Use a project ID that does not exist
        Long nonExistentProjectId = 999L;
        when(itemRepository.findUnassignedItemsByProjectId(nonExistentProjectId)).thenReturn(List.of());

        // Act: Call the service method
        List<Item> unassignedItems = itemService.getUnassignedItemsByProjectId(nonExistentProjectId);

        // Assert: Verify that the result is an empty list
        assertThat(unassignedItems).isEmpty();
    }

    @Test
    void updateItem_ShouldUpdateItem_WhenItemExists() {
        // Arrange: Set up mock item with initial values and stub the repository to
        // return the item by ID

        Item item = new TestDataFactory.ItemBuilder()
                .name("Old Name")
                .description("Old Description")
                .tags(List.of("Old tag 1", "Old tag 2"))
                .quantity(10)
                .orgUnit(mockOrgUnit).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Arrange: Create an UpdateItemDTO with updated values
        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder().build();

        // Stub the repository to return the item after saving
        when(itemRepository.save(item)).thenReturn(item);

        // Act: Update the item using the service
        Item updatedItem = itemService.updateItem(1L, itemDTO);

        // Assert: Verify that the item's name was updated correctly
        assertThat(updatedItem.getName()).isEqualTo(itemDTO.getName());
        assertThat(updatedItem.getDescription()).isEqualTo(itemDTO.getDescription());
        assertThat(updatedItem.getTags()).isEqualTo(itemDTO.getTags());
        assertThat(updatedItem.getQuantity()).isEqualTo(itemDTO.getQuantity());
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_ShouldThrowException_WhenItemDoesNotExist() {
        // Arrange: Stub the repository to return an empty result when searching for a
        // non-existent item
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        // Arrange: Set up an UpdateItemDTO with updated values
        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder().build();

        // Act & Assert: Attempt to update the item and expect a
        // ItemNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> itemService.updateItem(1L, itemDTO));
    }

    @Test
    void updateItem_ShouldNotChangeDescription_WhenDescriptionIsNull() {
        // Arrange: Set up a item with initial values
        Item item = new TestDataFactory.ItemBuilder()
                .name("Old Name")
                .description("Old Description")
                .tags(List.of("Old tag 1", "Old tag 2"))
                .quantity(10)
                .orgUnit(mockOrgUnit).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Stub the repository to return the item after saving
        when(itemRepository.save(item)).thenReturn(item);

        // Arrange: Set up an UpdateItemDTO with null description
        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder()
                .description(null)
                .build();

        // Act: Update item
        Item updatedItem = itemService.updateItem(1L, itemDTO);

        // Assert: Verify that the name was updated but the description remains the same
        assertThat(updatedItem.getName()).isEqualTo(itemDTO.getName());
        assertThat(updatedItem.getDescription()).isEqualTo("Old Description");
        assertThat(updatedItem.getTags()).isEqualTo(itemDTO.getTags());
        assertThat(updatedItem.getQuantity()).isEqualTo(itemDTO.getQuantity());
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_ShouldNotChangeTags_WhenTagsIsNull() {
        // Arrange: Set up a item with initial values
        List<String> oldTags = List.of("Old tag 1", "Old tag 2");
        Item item = new TestDataFactory.ItemBuilder()
                .name("Old Name")
                .description("Old Description")
                .tags(oldTags)
                .quantity(10)
                .orgUnit(mockOrgUnit).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Stub the repository to return the item after saving
        when(itemRepository.save(item)).thenReturn(item);

        // Arrange: Set up an UpdateItemDTO with null tags
        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder()
                .tags(null)
                .build();

        // Act: Update item
        Item updatedItem = itemService.updateItem(1L, itemDTO);

        // Assert: Verify that the name was updated but the tags remain the same
        assertThat(updatedItem.getName()).isEqualTo(itemDTO.getName());
        assertThat(updatedItem.getDescription()).isEqualTo(itemDTO.getDescription());
        assertThat(updatedItem.getTags()).isEqualTo(oldTags);
        assertThat(updatedItem.getQuantity()).isEqualTo(itemDTO.getQuantity());
        verify(itemRepository).save(item);
    }

    @Test
    void assignItemsToOrgUnit_Success() {
        // Arrange: Create target OrgUnit and mock items to move
        OrgUnit targetOrgUnit = new OrgUnit("Target OrgUnit", "Description", mockProject);
        when(orgUnitService.getOrgUnitById(10L)).thenReturn(targetOrgUnit);

        Item item1 = new TestDataFactory.ItemBuilder().project(mockProject).build();
        Item item2 = new TestDataFactory.ItemBuilder().project(mockProject).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        // Act: Move items in batch
        Iterable<Item> movedItems = itemService.assignItemsToOrgUnit(List.of(1L, 2L),
                10L);

        // Assert: Verify items are updated
        assertThat(movedItems).allMatch(item -> item.getOrgUnit().equals(targetOrgUnit));
    }

    @Test
    void assignItemsToOrgUnit_ItemNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange: Set up target OrgUnit ID and non-existent item ID
        OrgUnit targetOrgUnit = new OrgUnit("Target OrgUnit", "Description", mockProject);
        when(orgUnitService.getOrgUnitById(targetOrgUnit.getId())).thenReturn(targetOrgUnit);

        Long nonExistentItemId = 999L;
        when(itemRepository.findById(nonExistentItemId)).thenReturn(Optional.empty());

        // Act & Assert: Expect ItemNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            itemService.assignItemsToOrgUnit(List.of(nonExistentItemId), targetOrgUnit.getId());
        });
    }

    // TODO Allow partial success
    @Test
    void assignItemsToOrgUnit_DifferentProject_ShouldThrowIllegalArgumentException() {
        // Arrange: Stub the org unit to return mockOrgUnit when queried by ID
        when(orgUnitService.getOrgUnitById(mockOrgUnit.getId())).thenReturn(mockOrgUnit);

        // Arrange: Create an item with a different project than mockOrgUnit
        Project differentProject = new Project("Different Project", mockUser);
        Item itemWithDifferentProject = new TestDataFactory.ItemBuilder().project(differentProject).build();

        itemWithDifferentProject.setId(2L);
        when(itemRepository.findById(itemWithDifferentProject.getId()))
                .thenReturn(Optional.of(itemWithDifferentProject));

        // Act & Assert: Expect IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.assignItemsToOrgUnit(List.of(itemWithDifferentProject.getId()),
                    mockOrgUnit.getId());
        });
    }

    @Test
    void unassignItem_Success() {
        // Arrange: Create items and associate items with the mock orgUnit
        Item item1 = new TestDataFactory.ItemBuilder().orgUnit(mockOrgUnit).build();
        Item item2 = new TestDataFactory.ItemBuilder().orgUnit(mockOrgUnit).build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        // Act: Unassign the items from the orgUnit
        Iterable<Item> updatedItems = itemService.unassignItems(List.of(1L, 2L));

        // Assert: Verify that each item is no longer associated with the orgUnit
        for (Item updatedItem : updatedItems) {
            assertThat(updatedItem.getOrgUnit()).isNull();
        }
    }

    // TODO allow partial success
    @Test
    void unassignItems_ItemNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange: Set up item IDs, including a non-existent item ID
        List<Long> itemIds = List.of(999L);

        // Simulate ItemNotFoundException for the non-existent item
        when(itemRepository.findById(999L)).thenThrow(new ResourceNotFoundException(ResourceType.ITEM, 999L));

        // Act & Assert: Expect ItemNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            itemService.unassignItems(itemIds);
        });
    }

    @Test
    void deleteItem_ShouldDeleteItem_WhenItemExists() {
        // Arrange: Set up a item and stub the repository to return the item by ID
        Item item = new TestDataFactory.ItemBuilder().orgUnit(mockOrgUnit).build();
        Long itemId = item.getId();
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // Act: Delete the item using the service
        itemService.deleteItem(itemId);

        // Assert: Verify that the repository's delete method was called with the
        // correct ID
        verify(itemRepository).deleteById(itemId);
    }

    @Test
    void deleteItem_ShouldThrowException_WhenItemDoesNotExist() {
        // Arrange: Stub the repository to return an empty result when searching for a
        // non-existent item
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert: Attempt to delete the item and expect a
        // ItemNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> itemService.deleteItem(1L));

        // Assert: Verify that the repository's delete method was never called
        verify(itemRepository, never()).deleteById(anyLong());
    }
}
