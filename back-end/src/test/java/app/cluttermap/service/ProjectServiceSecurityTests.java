package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.repository.ProjectRepository;
import app.cluttermap.util.ResourceType;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnableTestcontainers
public class ProjectServiceSecurityTests {
    @Autowired
    private ProjectService projectService;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private SecurityService securityService;

    @MockBean
    private EventService eventService;

    private Project mockProject;

    @BeforeEach
    void setUp() {
        mockProject = createMockProject();

        when(securityService.isResourceOwner(anyLong(), any(ResourceType.class))).thenReturn(true);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Project should be retrieved successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void getProjectById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.PROJECT;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Act: Call the method under test
            Project project = projectService.getProjectById(resourceId);
            // Assert: Project should be retrieved successfully
            assertNotNull(project, description);
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> projectService.getProjectById(resourceId),
                    description);
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Project should be updated successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void updateProject_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.PROJECT;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        UpdateProjectDTO projectDTO = new TestDataFactory.UpdateProjectDTOBuilder().build();

        if (isOwner) {
            // Mock repository behavior for authorized access
            when(projectRepository.save(any(Project.class))).thenReturn(mockProject);

            // Arrange: Mock event logging
            mockLogUpdateEvent();

            // Act: Call the method under test
            Project project = projectService.updateProject(resourceId, projectDTO);

            // Assert: Validate successful update
            assertNotNull(project, description);
            verify(projectRepository).save(any(Project.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> projectService.updateProject(resourceId, projectDTO),
                    description);
            // Verify: Ensure project repository save is never invoked
            verify(projectRepository, never()).save(any(Project.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, Project should be deleted successfully when user has ownership",
            "false, AccessDeniedException should be thrown when user lacks ownership"
    })
    @WithMockUser(username = "testUser")
    void deleteById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        ResourceType resourceType = ResourceType.PROJECT;
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock repository behavior for authorized access
            doNothing().when(projectRepository).deleteById(1L);

            // Arrange: Mock event logging
            mockLogDeleteEvent();

            // Act: Call the method under test
            projectService.deleteProjectById(resourceId);

            // Assert: Validate successful deletion
            assertThatCode(() -> verify(projectRepository).deleteById(resourceId))
                    .as(description)
                    .doesNotThrowAnyException();
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> projectService.deleteProjectById(resourceId),
                    description);
            // Verify: Ensure project repository save is never invoked
            verify(projectRepository, never()).deleteById(1L);
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    private Project createMockProject() {
        User user = new User("mockProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(user).build();
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        return project;
    }

    private void mockLogUpdateEvent() {
        when(eventService.logUpdateEvent(any(), anyLong(), any())).thenReturn(new Event());
    }

    private void mockLogDeleteEvent() {
        when(eventService.logDeleteEvent(any(), anyLong())).thenReturn(new Event());
    }
}
