package app.cluttermap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import app.cluttermap.TestDataFactory;
import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.model.Item;
import app.cluttermap.model.OrgUnit;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.repository.ItemRepository;
import app.cluttermap.repository.OrgUnitRepository;
import app.cluttermap.repository.ProjectRepository;
import app.cluttermap.repository.RoomRepository;
import app.cluttermap.util.ResourceType;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class EntityResolutionServiceTests {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private OrgUnitRepository orgUnitRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private EntityResolutionService entityResolutionService;

    private User mockUser;

    private Project mockProject;

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
        mockUser.setId(1L);

        mockProject = new TestDataFactory.ProjectBuilder().user(mockUser).build();
    }

    @Test
    void resolveProject_ShouldReturnProject_WhenEntityTypeIsProject() {
        // Arrange
        when(projectRepository.findById(mockProject.getId())).thenReturn(Optional.of(mockProject));

        // Act
        Project resolvedProject = entityResolutionService.resolveProject(ResourceType.PROJECT, mockProject.getId());

        // Assert
        assertNotNull(resolvedProject);
        assertEquals(mockProject, resolvedProject);
        verify(projectRepository, times(1)).findById(mockProject.getId());
    }

    @Test
    void resolveProject_ShouldThrowException_WhenProjectNotFound() {
        // Arrange
        when(projectRepository.findById(mockProject.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> entityResolutionService.resolveProject(ResourceType.PROJECT, mockProject.getId()));
        verify(projectRepository, times(1)).findById(mockProject.getId());
    }

    @Test
    void resolveProject_ShouldReturnProject_WhenEntityTypeIsRoom() {
        // Arrange
        Room room = new TestDataFactory.RoomBuilder().project(mockProject).build();
        when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        // Act
        Project resolvedProject = entityResolutionService.resolveProject(ResourceType.ROOM, room.getId());

        // Assert
        assertNotNull(resolvedProject);
        assertEquals(mockProject, resolvedProject);
        verify(roomRepository, times(1)).findById(room.getId());
    }

    @Test
    void resolveProject_ShouldThrowException_WhenRoomNotFound() {
        // Arrange
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> entityResolutionService.resolveProject(ResourceType.ROOM, 1L));
        verify(roomRepository, times(1)).findById(1L);
    }

    @Test
    void resolveProject_ShouldReturnProject_WhenEntityTypeIsOrgUnit() {
        // Arrange
        OrgUnit orgUnit = new TestDataFactory.OrgUnitBuilder().project(mockProject).build();
        when(orgUnitRepository.findById(orgUnit.getId())).thenReturn(Optional.of(orgUnit));

        // Act
        Project resolvedProject = entityResolutionService.resolveProject(ResourceType.ORGANIZATIONAL_UNIT,
                orgUnit.getId());

        // Assert
        assertNotNull(resolvedProject);
        assertEquals(mockProject, resolvedProject);
        verify(orgUnitRepository, times(1)).findById(orgUnit.getId());
    }

    @Test
    void resolveProject_ShouldThrowException_WhenOrgUnitNotFound() {
        // Arrange
        when(orgUnitRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> entityResolutionService.resolveProject(ResourceType.ORGANIZATIONAL_UNIT, 1L));
        verify(orgUnitRepository, times(1)).findById(1L);
    }

    @Test
    void resolveProject_ShouldReturnProject_WhenEntityTypeIsItem() {
        // Arrange
        Item item = new TestDataFactory.ItemBuilder().project(mockProject).build();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        // Act
        Project resolvedProject = entityResolutionService.resolveProject(ResourceType.ITEM, item.getId());

        // Assert
        assertNotNull(resolvedProject);
        assertEquals(mockProject, resolvedProject);
        verify(itemRepository, times(1)).findById(item.getId());
    }

    @Test
    void resolveProject_ShouldThrowException_WhenItemNotFound() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> entityResolutionService.resolveProject(ResourceType.ITEM, 1L));
        verify(itemRepository, times(1)).findById(1L);
    }

}
