package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.cluttermap.TestDataFactory;
import app.cluttermap.model.Event;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.EntityHistoryDTO;
import app.cluttermap.repository.EventEntityRepository;
import app.cluttermap.repository.EventRepository;
import app.cluttermap.util.EventActionType;
import app.cluttermap.util.EventChangeType;
import app.cluttermap.util.ResourceType;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class EventServiceTests {

    @InjectMocks
    private EventService eventService;

    @Mock
    private ProjectService projectService;

    @Mock
    private RoomService roomService;

    @Mock
    private OrgUnitService orgUnitService;

    @Mock
    private ItemService itemService;

    @Mock
    private ProjectAccessService projectAccessService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventEntityRepository eventEntityRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private EntityResolutionService entityResolutionService;

    private Project mockProject;
    private User mockUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventService, "self", eventService);

        mockUser = new User("mockProviderId");
        mockUser.setUsername("mockUser");

        mockProject = new TestDataFactory.ProjectBuilder().user(mockUser).build();
        mockProject.setName("Test Project");
        mockProject.setOwner(mockUser);
    }

    @Test
    void getAllEventsInProject_ShouldReturnPaginatedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);
        List<Event> mockEvents = List.of(
                new Event(EventActionType.CREATE, mockProject, mockUser),
                new Event(EventActionType.UPDATE, mockProject, mockUser));
        Page<Event> mockPage = new PageImpl<>(mockEvents, pageable, 5);

        when(eventRepository.findAllEventsInProject(mockProject.getId(), pageable)).thenReturn(mockPage);

        // Act
        Page<Event> events = eventService.getAllEventsInProject(mockProject.getId(), 0, 2);

        // Assert
        assertThat(events.getContent()).hasSize(2);
        assertThat(events.getTotalElements()).isEqualTo(5);
        verify(eventRepository, times(1)).findAllEventsInProject(mockProject.getId(), pageable);
    }

    @Test
    void getEntityHistory_ShouldReturnPaginatedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);

        // Create mock data for EventHistoryDTO
        EntityHistoryDTO dto1 = new EntityHistoryDTO(
                ResourceType.ITEM,
                1L,
                EventChangeType.CREATE,
                "{\"field\":\"value\"}", // Mock JSON for details
                "testUser", // Username
                1L, // User ID
                Instant.parse("2025-01-02T14:30:50Z") // Mock timestamp
        );

        EntityHistoryDTO dto2 = new EntityHistoryDTO(
                ResourceType.ITEM,
                1L,
                EventChangeType.UPDATE,
                "{\"field\":\"newValue\"}", // Mock JSON for details
                "testUser", // Username
                1L, // User ID
                Instant.parse("2025-01-03T10:15:30Z") // Mock timestamp
        );

        List<EntityHistoryDTO> mockDTOs = List.of(dto1, dto2);
        Page<EntityHistoryDTO> mockPage = new PageImpl<>(mockDTOs, pageable, 4);

        when(eventEntityRepository.findHistoryByEntity(ResourceType.ROOM, 1L, pageable)).thenReturn(mockPage);

        // Act
        Page<EntityHistoryDTO> events = eventService.getEntityHistory(ResourceType.ROOM, 1L, 0, 2);

        // Assert
        assertThat(events.getContent()).hasSize(2);
        assertThat(events.getTotalElements()).isEqualTo(4);
        assertThat(events.getContent()).containsExactly(dto1, dto2);

        verify(eventEntityRepository, times(1)).findHistoryByEntity(ResourceType.ROOM, 1L, pageable);
    }

    @Test
    public void testLogCreateRoomEvent() {
        // Arrange
        User user = createMockUser();
        when(securityService.getCurrentUser()).thenReturn(user);

        Project project = new TestDataFactory.ProjectBuilder().user(user).build();

        Room room = new TestDataFactory.RoomBuilder().id(2L).name("Room 1").description("Test room").project(project)
                .build();

        when(entityResolutionService.resolveProject(any(ResourceType.class), anyLong())).thenReturn(project);

        // Mock save behavior for event repository
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Build the expected payload
        Map<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("name", room.getName());
        expectedPayload.put("description", room.getDescription());

        // Act
        Event event = eventService.logEvent(
                ResourceType.ROOM, room.getId(),
                EventActionType.CREATE, expectedPayload);

        // Assert
        assertEventFields(event, EventActionType.CREATE, user);
        assertEquals(project, event.getProject());
        verify(eventRepository, times(1)).save(event);
        verify(entityResolutionService, times(1)).resolveProject(any(ResourceType.class), anyLong());
    }

    @Test
    public void testLogUpdateEvent() {
        // Arrange
        User user = createMockUser();
        when(securityService.getCurrentUser()).thenReturn(user);

        Project project = new TestDataFactory.ProjectBuilder().user(user).build();

        Room oldRoom = new TestDataFactory.RoomBuilder().id(2L).name("Old Room").description("Old Description")
                .project(project).build();

        Room newRoom = new TestDataFactory.RoomBuilder().id(2L).name("New Room").description("New Description")
                .project(project).build();

        when(entityResolutionService.resolveProject(ResourceType.ROOM, oldRoom.getId())).thenReturn(project);

        // Mock save behavior for event repository
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Build the expected payload
        Map<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("name", newRoom.getName());
        expectedPayload.put("description", newRoom.getDescription());
        // Act
        Event event = eventService.logEvent(
                ResourceType.ROOM, oldRoom.getId(),
                EventActionType.UPDATE, expectedPayload);

        // Assert
        assertEventFields(event, EventActionType.UPDATE, user);
        assertEquals(project, event.getProject());
        verify(eventRepository, times(1)).save(event);
        verify(entityResolutionService, times(1)).resolveProject(ResourceType.ROOM, oldRoom.getId());
    }

    @Test
    public void testConvertToJson() throws Exception {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("key1", "value1");
        testMap.put("key2", 123);

        String json = eventService.convertToJson(testMap);
        System.out.println(json);

        // Parse back to a Map to verify contents
        ObjectMapper objectMapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = objectMapper.readValue(json, Map.class);

        assertEquals(testMap, resultMap); // Assert that the input map equals the output map
    }

    @Test
    public void testDetectChanges_SomeFieldsChanged() {
        // Arrange
        Project project = new TestDataFactory.ProjectBuilder().user(createMockUser()).build();
        Room oldRoom = new TestDataFactory.RoomBuilder()
                .name("Old Room")
                .description("Old Description")
                .project(project)
                .build();

        Room newRoom = new TestDataFactory.RoomBuilder()
                .name("Old Room") // Unchanged
                .description("New Description") // Changed
                .project(project)
                .build();

        // Act
        Map<String, Object> changes = eventService.detectChanges(oldRoom, newRoom);

        // Assert
        assertEquals(1, changes.size());
        assertEquals("New Description", changes.get("description"));
    }

    @Test
    public void testDetectChanges_AddedField() {
        // Arrange
        Project project = new TestDataFactory.ProjectBuilder().user(createMockUser()).build();
        Item oldItem = new TestDataFactory.ItemBuilder()
                .name("Item 1")
                .description(null) // Field not set
                .project(project)
                .build();

        Item newItem = new TestDataFactory.ItemBuilder()
                .name("Item 1") // Unchanged
                .description("New Description") // Added
                .project(project)
                .build();

        // Act
        Map<String, Object> changes = eventService.detectChanges(oldItem, newItem);

        // Assert
        assertEquals(1, changes.size());
        assertEquals("New Description", changes.get("description"));
    }

    @Test
    public void testDetectChanges_RemovedField() {
        // Arrange
        Project project = new TestDataFactory.ProjectBuilder().user(createMockUser()).build();
        OrgUnit oldOrgUnit = new TestDataFactory.OrgUnitBuilder()
                .name("Org Unit 1")
                .description("Description 1") // Field to be removed
                .project(project)
                .build();

        OrgUnit newOrgUnit = new TestDataFactory.OrgUnitBuilder()
                .name("Org Unit 1") // Unchanged
                .description(null) // Field removed
                .project(project)
                .build();

        // Act
        Map<String, Object> changes = eventService.detectChanges(oldOrgUnit, newOrgUnit);

        // Assert
        assertEquals(1, changes.size());
        assertEquals(null, changes.get("description"));
    }

    @Test
    public void testDetectChanges_NoChanges() {
        // Arrange
        Project project = new TestDataFactory.ProjectBuilder().user(createMockUser()).build();
        Room oldRoom = new TestDataFactory.RoomBuilder()
                .name("Room 1")
                .description("Description 1")
                .project(project)
                .build();

        Room newRoom = new TestDataFactory.RoomBuilder()
                .name("Room 1") // Unchanged
                .description("Description 1") // Unchanged
                .project(project)
                .build();

        // Act
        Map<String, Object> changes = eventService.detectChanges(oldRoom, newRoom);

        // Assert
        assertEquals(0, changes.size()); // No changes
    }

    private User createMockUser() {
        User mockUser = new User("mockProviderId");
        return mockUser;
    }

    private void assertEventFields(Event event, EventActionType action, User user) {
        assertEquals(action, event.getAction());
        assertEquals(user, event.getUser());
        assertNotNull(event.getTimestamp());
    }
}
