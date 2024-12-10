package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import app.cluttermap.repository.EventRepository;
import app.cluttermap.util.EventActionType;
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
    private EventRepository eventRepository;

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
                new Event(ResourceType.ROOM, 1L, EventActionType.CREATE, null, mockProject, mockUser),
                new Event(ResourceType.ROOM, 2L, EventActionType.UPDATE, null, mockProject, mockUser));
        Page<Event> mockPage = new PageImpl<>(mockEvents, pageable, 5);

        when(eventRepository.findAllEventsInProject(mockProject, pageable)).thenReturn(mockPage);

        // Act
        Page<Event> events = eventService.getAllEventsInProject(mockProject, 0, 2);

        // Assert
        assertThat(events.getContent()).hasSize(2);
        assertThat(events.getTotalElements()).isEqualTo(5);
        assertThat(events.getContent().get(0).getEntityType()).isEqualTo(ResourceType.ROOM);
        verify(eventRepository, times(1)).findAllEventsInProject(mockProject, pageable);
    }

    @Test
    void getEventsForEntity_ShouldReturnPaginatedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);
        List<Event> mockEvents = List.of(
                new Event(ResourceType.ROOM, 1L, EventActionType.CREATE, null, mockProject, mockUser),
                new Event(ResourceType.ROOM, 1L, EventActionType.UPDATE, null, mockProject, mockUser));
        Page<Event> mockPage = new PageImpl<>(mockEvents, pageable, 4);

        when(eventRepository.findByEntityTypeAndEntityId(ResourceType.ROOM, 1L, pageable)).thenReturn(mockPage);

        // Act
        Page<Event> events = eventService.getEventsForEntity(ResourceType.ROOM, 1L, 0, 2);

        // Assert
        assertThat(events.getContent()).hasSize(2);
        assertThat(events.getTotalElements()).isEqualTo(4);
        assertThat(events.getContent().get(0).getEntityId()).isEqualTo(1L);
        verify(eventRepository, times(1)).findByEntityTypeAndEntityId(ResourceType.ROOM, 1L, pageable);
    }

    @Test
    void getChangedEntitiesSince_ShouldReturnMappedResults() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<Object[]> mockResults = List.of(
                new Object[] { ResourceType.ROOM, 1L },
                new Object[] { ResourceType.ROOM, 2L },
                new Object[] { ResourceType.PROJECT, 1L });

        when(eventRepository.findChangesSince(since)).thenReturn(mockResults);

        // Act
        Map<ResourceType, Set<Long>> changes = eventService.getChangedEntitiesSince(since);

        // Assert
        assertThat(changes).hasSize(2);
        assertThat(changes.get(ResourceType.ROOM)).containsExactlyInAnyOrder(1L, 2L);
        assertThat(changes.get(ResourceType.PROJECT)).containsExactly(1L);
        verify(eventRepository, times(1)).findChangesSince(since);
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

        // Act
        Event event = eventService.logCreateEvent(ResourceType.ROOM, room.getId(), room);

        // Assert
        assertEventFields(event, ResourceType.ROOM, 2L, EventActionType.CREATE, user);
        assertEquals("{\"id\":2,\"name\":\"Room 1\",\"description\":\"Test room\",\"orgUnits\":[]}",
                event.getPayload());
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

        // Act
        Event event = eventService.logUpdateEvent(ResourceType.ROOM, oldRoom.getId(), oldRoom, newRoom);

        // Assert
        assertEventFields(event, ResourceType.ROOM, 2L, EventActionType.UPDATE, user);
        assertEquals("{\"name\":\"New Room\",\"description\":\"New Description\"}", event.getPayload());
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

    private void assertEventFields(Event event, ResourceType entityType, Long entityId, EventActionType action,
            User user) {
        assertEquals(entityType, event.getEntityType());
        assertEquals(entityId, event.getEntityId());
        assertEquals(action, event.getAction());
        assertEquals(user, event.getUser());
        assertNotNull(event.getTimestamp());
    }
}
