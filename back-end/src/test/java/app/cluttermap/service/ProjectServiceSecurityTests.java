package app.cluttermap.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.repository.ProjectRepository;

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

    private Project mockProject;

    @BeforeEach
    void setUp() {
        mockProject = createMockProject();

        when(securityService.isResourceOwner(anyLong(), anyString())).thenReturn(true);
    }

    @ParameterizedTest
    @CsvSource({
            "true, getProjectById_UserHasOwnership",
            "false, getProjectById_UserLacksOwnership"
    })
    @WithMockUser(username = "testUser")
    void getProjectById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "project";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Act: Call the method under test
            Project project = projectService.getProjectById(resourceId);
            // Assert: Project should be retrieved successfully
            assertNotNull(project, "Project should not be null when the user has ownership.");
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> projectService.getProjectById(resourceId),
                    "AccessDeniedException should be thrown when the user lacks ownership.");
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, updateProject_UserHasOwnership",
            "false, updateProject_UserLacksOwnership"
    })
    @WithMockUser(username = "testUser")
    void updateProject_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "project";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        UpdateProjectDTO projectDTO = new TestDataFactory.UpdateProjectDTOBuilder().build();

        if (isOwner) {
            // Mock repository behavior for authorized access
            when(projectRepository.save(any(Project.class))).thenReturn(mockProject);

            // Act: Call the method under test
            Project project = projectService.updateProject(resourceId, projectDTO);

            // Assert: Validate successful update
            assertNotNull(project, "Project should not be null when the user has ownership.");
            verify(projectRepository).save(any(Project.class));
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> projectService.updateProject(resourceId, projectDTO),
                    "AccessDeniedException should be thrown when the user lacks ownership.");
            // Verify: Ensure project repository save is never invoked
            verify(projectRepository, never()).save(any(Project.class));
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    @ParameterizedTest
    @CsvSource({
            "true, deleteById_UserHasOwnership",
            "false, deleteById_UserLacksOwnership"
    })
    @WithMockUser(username = "testUser")
    void deleteById_ShouldRespectOwnership(boolean isOwner, String description) {
        // Arrange: Prepare mock data and configure security service
        Long resourceId = 1L;
        String resourceType = "project";
        when(securityService.isResourceOwner(resourceId, resourceType)).thenReturn(isOwner);

        if (isOwner) {
            // Mock repository behavior for authorized access
            doNothing().when(projectRepository).deleteById(1L);

            // Act: Call the method under test
            projectService.deleteProjectById(resourceId);

            // Assert: Validate successful deletion
            verify(projectRepository).deleteById(resourceId);
        } else {
            // Act & Assert: Validate access denial
            assertThrows(AccessDeniedException.class,
                    () -> projectService.deleteProjectById(resourceId),
                    "AccessDeniedException should be thrown when the user lacks ownership.");
            // Verify: Ensure project repository save is never invoked
            verify(projectRepository, never()).deleteById(1L);
        }

        // Verify: Ensure ownership check was invoked
        verify(securityService).isResourceOwner(resourceId, resourceType);
    }

    private Project createMockProject() {
        User user = new User("mockProviderId");
        Project project = new TestDataFactory.ProjectBuilder().user(user).build();
        project.setId(1L);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        return project;
    }

}
