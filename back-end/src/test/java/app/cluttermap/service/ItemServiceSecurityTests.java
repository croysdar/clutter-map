package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.EnableTestcontainers;
import app.cluttermap.TestDataFactory;
import app.cluttermap.model.Event;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewItemDTO;
import app.cluttermap.model.dto.UpdateItemDTO;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.util.ResourceType;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableTestcontainers
public class ItemServiceSecurityTests {
    @Autowired
    private ItemService itemService;

    @MockBean
    private OrgUnitService orgUnitService;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private OrgUnitRepository orgUnitRepository;

    @MockBean
    private EventService eventService;

    @MockBean
    private SecurityService securityService;

    private Project mockProject;
    private Item mockItem;

    @BeforeEach
    void setUp() {
        mockProject = createMockProject();
        mockItem = createMockItem(mockProject);
        when(securityService.isResourceOwner(anyLong(), any(ResourceType.class))).thenReturn(true);
        // when(projectService.getProjectById(mockProject.getId())).thenReturn(mockProject);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Item should be retrieved successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void getItemById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.ITEM;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Act: Call the method under test
            Item item = itemService.getItemById(resourceId);
            // Assert: Item should be retrieved successfully
            assertNotNull(item, description);
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> itemService.getItemById(resourceId),
                    description);
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Unassigned items should be retrieved when user has ownership of the project",
            "false, AccessDeniedException should be thrown when user lacks ownership of the project"
    })
    @WithMockUser(username = "testUser")
    void getUnassignedItemsByProjectId_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.PROJECT;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock repository behavior for authorized access
            when(itemRepository.findUnassignedItemsByProjectId(mockProject.getId())).thenReturn(List.of(mockItem));

            // Act: Call the method under test
            List<Item> items = itemService.getUnassignedItemsByProjectId(mockProject.getId());

            // Assert: Validate retrieved items
            assertAll(() -> assertNotNull(items, description));
            verify(itemRepository).findUnassignedItemsByProjectId(resourceId);

        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> itemService.getUnassignedItemsByProjectId(mockProject.getId()),
                    description);
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, PROJECT, Item should be created successfully when user has ownership of the project",
            "false, PROJECT, AccessDeniedException should be thrown when user lacks ownership of the project",
            "true, ORGANIZATIONAL_UNIT, Item should be created successfully when user has ownership of the org unit",
            "false, ORGANIZATIONAL_UNIT, AccessDeniedException should be thrown when user lacks ownership of the org unit",
    })
    @WithMockUser(username = "testUser")
    void createItem_ShouldRespectOwnership(boolean isOwner, ResourceType resourceType, String description) {
        // Arrange: Prepare mock data based on the resource type
        Long resourceId;
        NewItemDTO itemDTO;

        if (resourceType.equals(ResourceType.PROJECT)) {
            resourceId = mockProject.getId();
            itemDTO = new TestDataFactory.NewItemDTOBuilder()
                    .projectId(mockProject.getId())
                    .orgUnitId(null)
                    .build();
        } else {
            OrgUnit mockOrgUnit = createMockOrgUnit(mockProject);
            resourceId = mockOrgUnit.getId();
            itemDTO = new TestDataFactory.NewItemDTOBuilder()
                    .projectId(null)
                    .orgUnitId(mockOrgUnit.getId())
                    .build();
        }

        // Arrange: Configure security service
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);
        if (isOwner) {
            // Mock repository behavior for authorized access
            when(itemRepository.save(any(Item.class))).thenReturn(mockItem);

            // Arrange: Mock event logging
            mockLogEvent();

            // Act: Call the method under test
            Item item = itemService.createItem(itemDTO);

            // Assert: Validate item creation
            assertNotNull(item, description);
            verify(itemRepository).save(any(Item.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> itemService.createItem(itemDTO),
                    description);
            // Verify: Ensure item repository save is never invoked
            verify(itemRepository, never()).save(any(Item.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Item should be updated successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void updateItem_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.ITEM;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        UpdateItemDTO itemDTO = new TestDataFactory.UpdateItemDTOBuilder().build();

        if (isOwner) {
            // Mock repository behavior for authorized access
            when(itemRepository.save(any(Item.class))).thenReturn(mockItem);

            // Arrange: Mock event logging
            mockLogEvent();

            // Act: Call the method under test
            Item item = itemService.updateItem(resourceId, itemDTO);

            // Assert: Validate successful update
            assertNotNull(item, description);
            verify(itemRepository).save(any(Item.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> itemService.updateItem(resourceId, itemDTO),
                    description);
            // Verify: Ensure item repository save is never invoked
            verify(itemRepository, never()).save(any(Item.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Items should be assigned to org unit successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void assignItemsToOrgUnit_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.ITEM;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        OrgUnit mockOrgUnit = createMockOrgUnit(mockProject);

        if (isOwner) {
            // Act: Call the method under test
            Iterable<Item> items = itemService.assignItemsToOrgUnit(List.of(resourceId), mockOrgUnit.getId());

            // Assert: Validate successful assignment
            assertAll(
                    () -> assertNotNull(items, "Items list should not be null when the user has ownership."),
                    () -> assertEquals(mockOrgUnit, mockItem.getOrgUnit(), description));

            // Verify: Ensure org unit retrieval occurred
            verify(orgUnitService).getOrgUnitById(mockOrgUnit.getId());
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> itemService.assignItemsToOrgUnit(List.of(resourceId), mockOrgUnit.getId()),
                    description);

            // Verify: Ensure item repository save is never invoked
            verify(itemRepository, never()).save(any(Item.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Items should be unassigned successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void unassignItems_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.ITEM;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock interaction with item repository for authorized access
            // Ensure item is set up properly for unassignment
            mockItemWithOrgUnit();

            // Act: Call the method under test
            Iterable<Item> items = itemService.unassignItems(List.of(1L));

            // Assert: Validate successful unassignment
            assertNotNull(items, description);
            verify(orgUnitRepository).save(any(OrgUnit.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> itemService.unassignItems(List.of(resourceId)),
                    description);

            // Verify: Ensure item repository save is never invoked
            verify(itemRepository, never()).save(any(Item.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Item should be deleted successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void deleteById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.ITEM;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock repository behavior for authorized access
            doNothing().when(itemRepository).deleteById(1L);

            // Arrange: Mock event logging
            mockLogEvent();

            // Act: Call the method under test
            itemService.deleteItemById(1L);

            // Assert: Validate successful deletion
            assertThatCode(() -> verify(itemRepository).deleteById(1L))
                    .as(description)
                    .doesNotThrowAnyException();

        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> itemService.deleteItemById(resourceId),
                    description);
            // Verify: Ensure item repository save is never invoked
            verify(itemRepository, never()).deleteById(1L);
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    private Project createMockProject() {
        User user = new User("mockProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(user).build();
        when(projectService.getProjectById(project.getId())).thenReturn(project);

        return project;
    }

    private OrgUnit createMockOrgUnit(Project project) {
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().project(project).build();
        when(orgUnitService.getOrgUnitById(orgUnit.getId())).thenReturn(orgUnit);

        return orgUnit;
    }

    private Item createMockItem(Project project) {
        Item item = new TestDataFactory.ItemBuilder().project(project).build();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        return item;
    }

    private void mockItemWithOrgUnit() {
        OrgUnit mockOrgUnit = createMockOrgUnit(mockProject);
        mockItem.setOrgUnit(mockOrgUnit);
        when(itemRepository.findById(mockItem.getId())).thenReturn(Optional.of(mockItem));
    }

    private void mockLogEvent() {
        when(eventService.logEvent(any(), anyLong(), any(), any())).thenReturn(new Event());
    }

}
