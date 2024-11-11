package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.exception.item.ItemLimitReachedException;
import app.cluttermap.exception.item.ItemNotFoundException;
import app.cluttermap.exception.org_unit.OrgUnitNotFoundException;
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
    void getItemId_ShouldReturnItem_WhenItemExists() {
        // Arrange: Set up a sample org unit and stub the repository to return it by ID
        Item item = new Item("Sample Item", "Item description", List.of(), mockOrgUnit);
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
        assertThrows(ItemNotFoundException.class, () -> itemService.getItemById(1L));
    }

    @Test
    void createItem_ShouldCreateItem_WhenRoomExists() {
        // Arrange: Stub org unit retrieval to return mockOrgUnit when the specified ID
        // is used
        when(orgUnitService.getOrgUnitById(1L)).thenReturn(mockOrgUnit);

        // Arrange: Prepare the Item DTO with the room ID as a string
        NewItemDTO itemDTO = new NewItemDTO("New Item", "Item description", List.of(), String.valueOf(1L));

        // Arrange: Create a mock Item that represents the saved item returned by
        // the repository
        Item mockItem = new Item(itemDTO.getName(), itemDTO.getDescription(), List.of(), mockOrgUnit);
        when(itemRepository.save(any(Item.class))).thenReturn(mockItem);

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
    void createItem_ShouldThrowException_WhenOrgUnitDoesNotExist() {
        // Arrange: Set up the DTO with a org unit ID that doesn't exist
        NewItemDTO itemDTO = new NewItemDTO("New Item", "Item description", List.of(), "999");
        when(orgUnitService.getOrgUnitById(itemDTO.getOrgUnitIdAsLong())).thenThrow(new OrgUnitNotFoundException());

        // Act & Assert: Attempt to create the item and expect a
        // RoomNotFoundException
        assertThrows(OrgUnitNotFoundException.class, () -> itemService.createItem(itemDTO));
    }

    @Disabled("Feature under development")
    @Test
    void createItem_ShouldThrowException_WhenItemLimitReached() {
        // Arrange: Set up a room with the maximum allowed items
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < ITEM_LIMIT; i++) {
            items.add(new Item("Item " + (i + 1), "Description " + (i + 1), List.of(), mockOrgUnit));
        }
        mockOrgUnit.setItems(items);

        // Stub the repository to return the room and items
        when(projectService.getProjectById(1L)).thenReturn(mockProject);
        when(itemRepository.findByOwnerId(1L)).thenReturn(items);

        NewItemDTO itemDTO = new NewItemDTO("Extra Item", "Description", List.of(), String.valueOf(1L));

        // Act & Assert: Attempt to create a item and expect an exception
        assertThrows(ItemLimitReachedException.class, () -> itemService.createItem(itemDTO));
    }

    @Test
    void getUserItems_ShouldReturnItemsOwnedByUser() {
        // Arrange: Set up mock user, projects, and items, and stub the repository to
        // return items owned by the user
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        Item item1 = new Item("Item 1", "Item description 1", List.of(), mockOrgUnit);
        Item item2 = new Item("Item 2", "Item description 2", List.of(), mockOrgUnit);
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

        Item item1 = new Item("Item 1", "Description 1", List.of(), orgUnit1);
        Item item2 = new Item("Item 2", "Description 2", List.of(), orgUnit2);

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
    void updateItem_ShouldUpdateItem_WhenItemExists() {
        // Arrange: Set up mock item with initial values and stub the repository to
        // return the item by ID
        Item item = new Item("Old Name", "Old Description", List.of("tag 1", "tag 2"), mockOrgUnit);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Arrange: Create an UpdateItemDTO with updated values
        UpdateItemDTO itemDTO = new UpdateItemDTO("Updated Name", "Updated Description",
                List.of("Updated tag 1", "Updated tag 2"));

        // Stub the repository to return the item after saving
        when(itemRepository.save(item)).thenReturn(item);

        // Act: Update the item using the service
        Item updatedItem = itemService.updateItem(1L, itemDTO);

        // Assert: Verify that the item's name was updated correctly
        assertThat(updatedItem.getName()).isEqualTo("Updated Name");
        assertThat(updatedItem.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedItem.getTags()).isEqualTo(List.of("Updated tag 1", "Updated tag 2"));
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_ShouldThrowException_WhenItemDoesNotExist() {
        // Arrange: Stub the repository to return an empty result when searching for a
        // non-existent item
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        // Arrange: Set up an UpdateItemDTO with updated values
        UpdateItemDTO itemDTO = new UpdateItemDTO("Updated Name", "Updated Description", List.of("tag 1", "tag 2"));

        // Act & Assert: Attempt to update the item and expect a
        // ItemNotFoundException
        assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(1L, itemDTO));
    }

    @Test
    void updateItem_ShouldNotChangeDescription_WhenDescriptionIsNull() {
        // Arrange: Set up a item with an initial description
        Item item = new Item("Item Name", "Initial Description", List.of("tag 1", "tag 2"), mockOrgUnit);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Stub the repository to return the item after saving
        when(itemRepository.save(item)).thenReturn(item);

        // Arrange: Set up an UpdateItemDTO with null description
        UpdateItemDTO itemDTO = new UpdateItemDTO("Updated Name", null, List.of("Updated tag 1", "Updated tag 2"));

        // Act: Update item
        Item updatedItem = itemService.updateItem(1L, itemDTO);

        // Assert: Verify that the name was updated but the description remains the same
        assertThat(updatedItem.getName()).isEqualTo("Updated Name");
        assertThat(updatedItem.getDescription()).isEqualTo("Initial Description");
        assertThat(updatedItem.getTags()).isEqualTo(List.of("Updated tag 1", "Updated tag 2"));
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_ShouldNotChangeTags_WhenTagsIsNull() {
        // Arrange: Set up a item with an initial description
        Item item = new Item("Item Name", "Description", List.of("tag 1", "tag 2"), mockOrgUnit);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Stub the repository to return the item after saving
        when(itemRepository.save(item)).thenReturn(item);

        // Arrange: Set up an UpdateItemDTO with null tags
        UpdateItemDTO itemDTO = new UpdateItemDTO("Updated Name", "Updated Description", null);

        // Act: Update item
        Item updatedItem = itemService.updateItem(1L, itemDTO);

        // Assert: Verify that the name was updated but the tags remain the same
        assertThat(updatedItem.getName()).isEqualTo("Updated Name");
        assertThat(updatedItem.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedItem.getTags()).isEqualTo(List.of("tag 1", "tag 2"));
        verify(itemRepository).save(item);
    }

    @Test
    void moveItemBetweenOrgUnits_Success() {
        // Arrange: Create a project, orgUnits, and an item
        OrgUnit sourceOrgUnit = new OrgUnit("Source OrgUnit", "Description", mockProject);
        OrgUnit targetOrgUnit = new OrgUnit("Target OrgUnit", "Description", mockProject);

        Item item = new Item("Test Item", "Item Description", List.of("tag1"), sourceOrgUnit);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(orgUnitRepository.findById(targetOrgUnit.getId())).thenReturn(Optional.of(targetOrgUnit));

        // Act: Move the item from sourceOrgUnit to targetOrgUnit
        Item updatedItem = itemService.moveItemBetweenOrgUnits(item.getId(), targetOrgUnit.getId());

        // Assert: Verify the item is now associated with the targetOrgUnit
        assertThat(updatedItem.getOrgUnit()).isEqualTo(targetOrgUnit);
        assertThat(sourceOrgUnit.getItems()).doesNotContain(item);
        assertThat(targetOrgUnit.getItems()).contains(item);
    }

    @Test
    void moveItemBetweenOrgUnits_DifferentProjects_ShouldThrowIllegalArgumentException() {
        // Arrange: Create two projects, each with its own orgUnit
        Project project1 = new Project("Project 1", mockUser);
        Project project2 = new Project("Project 2", mockUser);

        OrgUnit orgUnit1 = new OrgUnit("OrgUnit 1", "Description", project1);
        OrgUnit orgUnit2 = new OrgUnit("OrgUnit 2", "Description", project2);

        Item item = new Item("Test Item", "Item Description", List.of("tag1"), orgUnit1);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(orgUnitRepository.findById(orgUnit2.getId())).thenReturn(Optional.of(orgUnit2));

        // Act & Assert: Attempt to move the item to an orgUnit in a different project
        assertThrows(IllegalArgumentException.class, () -> {
            itemService.moveItemBetweenOrgUnits(item.getId(), orgUnit2.getId());
        });
    }

    @Test
    void moveItemBetweenOrgUnits_ItemNotFound_ShouldThrowItemNotFoundException() {
        // Arrange: Ensure no item exists with the given ID
        Long nonExistentItemId = 999L;

        // Act & Assert: Attempt to move a non-existent item
        assertThrows(ItemNotFoundException.class, () -> {
            itemService.moveItemBetweenOrgUnits(nonExistentItemId, 1L);
        });
    }

    @Test
    void moveItemBetweenOrgUnits_OrgUnitNotFound_ShouldThrowOrgUnitNotFoundException() {
        // Arrange: Create an item
        Project project = new Project("Test Project", mockUser);

        OrgUnit orgUnit = new OrgUnit("Source OrgUnit", "Description", project);

        Item item = new Item("Test Item", "Item Description", List.of("tag1"), orgUnit);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        // Act & Assert: Attempt to move the item to a non-existent orgUnit
        Long nonExistentOrgUnitId = 999L;
        assertThrows(OrgUnitNotFoundException.class, () -> {
            itemService.moveItemBetweenOrgUnits(item.getId(), nonExistentOrgUnitId);
        });
    }

    @Test
    void deleteItem_ShouldDeleteItem_WhenItemExists() {
        // Arrange: Set up a item and stub the repository to return the item by ID
        Item item = new Item("Sample Item", "Item Description", List.of(), mockOrgUnit);
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
        assertThrows(ItemNotFoundException.class, () -> itemService.deleteItem(1L));

        // Assert: Verify that the repository's delete method was never called
        verify(itemRepository, never()).deleteById(anyLong());
    }
}
