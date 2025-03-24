package app.cluttermap.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import app.cluttermap.TestDataFactory;
import app.cluttermap.exception.ResourceNotFoundException;
import app.cluttermap.exception.project.ProjectLimitReachedException;
import app.cluttermap.model.Event;
import app.cluttermap.model.Project;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewProjectDTO;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.repository.ProjectRepository;
import app.cluttermap.util.EventChangeType;
import app.cluttermap.util.ResourceType;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ProjectServiceTests {
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private EventService eventService;

    @InjectMocks
    private ProjectService projectService;

    private User mockUser;

    private static int PROJECT_LIMIT = 3;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(projectService, "self", projectService);

        mockUser = new User("mockProviderId");
    }

    @Test
    void getUserProjects_ShouldReturnProjectsOwnedByUser() {
        // Arrange: Mock the current user and projects
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        Project project1 = new TestDataFactory.ProjectBuilder().user(mockUser).build();
        Project project2 = new TestDataFactory.ProjectBuilder().user(mockUser).build();
        when(projectRepository.findByOwnerId(mockUser.getId())).thenReturn(List.of(project1, project2));

        // Act: Call service method
        Iterable<Project> userProjects = projectService.getUserProjects();

        // Assert: Verify that the returned projects match the expected projects
        assertThat(userProjects).containsExactly(project1, project2)
                .as("Projects owned by user should be returned when they exist");

        // Verify dependencies are called as expected
        verify(securityService).getCurrentUser();
        verify(projectRepository).findByOwnerId(mockUser.getId());
    }

    @Test
    void getUserProjects_ShouldReturnEmptyList_WhenNoProjectsExist() {
        // Arrange: Mock the current user and an empty repository result
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(projectRepository.findByOwnerId(mockUser.getId())).thenReturn(Collections.emptyList());

        // Act: Call service method
        Iterable<Project> userProjects = projectService.getUserProjects();

        // Assert: Verify that the returned projects list is empty
        assertThat(userProjects)
                .as("Empty list should be returned when user owns no projects")
                .isEmpty();

        // Verify dependencies are called as expected
        verify(securityService).getCurrentUser();
        verify(projectRepository).findByOwnerId(mockUser.getId());
    }

    @ParameterizedTest
    @CsvSource({
            "true, Project should be returned when it exists",
            "false, ResourceNotFoundException should be thrown when project project not exist"
    })
    void getProjectById_ShouldHandleExistenceCorrectly(boolean projectExists, String description) {
        // Arrange
        Long resourceId = 1L;
        if (projectExists) {
            // Arrange: Mock the repository to return an project
            Project mockProject = mockProjectInRepository(resourceId);

            // Act: Call service method
            Project foundProject = projectService.getProjectById(resourceId);

            // Assert: Verify the project retrieved matches the mock
            assertThat(foundProject)
                    .as(description)
                    .isNotNull()
                    .isEqualTo(mockProject);

        } else {
            // Arrange: Mock the repository to return empty
            mockNonexistentProjectInRepository(resourceId);

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> projectService.getProjectById(resourceId),
                    description);
        }

        // Verify: Ensure repository interaction occurred
        verify(projectRepository).findById(anyLong());
    }

    @Test
    void createProject_ShouldCreateProject_WhenValid() {
        // Arrange: Set up mocks for the current user with no existing project
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        when(projectRepository.findByOwnerId(mockUser.getId())).thenReturn(Collections.emptyList());

        // Arrange: Create a DTO for the new project and set up a mock project to return
        // on save
        NewProjectDTO projectDTO = new TestDataFactory.NewProjectDTOBuilder().build();
        Project mockProject = new TestDataFactory.ProjectBuilder().fromDTO(projectDTO).user(mockUser).build();
        when(projectRepository.save(any(Project.class))).thenReturn(mockProject);

        // Arrange: Mock event logging
        mockLogEvent();

        // Act: Call service method
        Project createdProject = projectService.createProject(projectDTO);

        // Assert: Validate the created project
        assertThat(createdProject)
                .as("Project should be created when valid")
                .isNotNull()
                .isEqualTo(mockProject);

        // Verify that the correct service and repository methods were called
        verify(securityService).getCurrentUser();
        verify(projectRepository).save(any(Project.class));

        // Capture and verify the arguments passed to logUpdateEvent
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        // Assert: Verify event logging
        verify(eventService).logEvent(
                eq(ResourceType.PROJECT), eq(mockProject.getId()),
                eq(EventChangeType.CREATE), payloadCaptor.capture());

        // Assert: Verify the payload contains the expected values
        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload)
                .containsEntry("name", createdProject.getName());
    }

    @Test
    void createProject_ShouldThrowException_WhenLimitReached() {
        // Arrange: Set up the current user and mock their existing projects to be one
        // less than the project limit
        when(securityService.getCurrentUser()).thenReturn(mockUser);

        List<Project> existingProjects = new ArrayList<>();
        for (int i = 0; i < PROJECT_LIMIT - 1; i++) {
            existingProjects.add(new TestDataFactory.ProjectBuilder().user(mockUser).build());
        }
        when(projectRepository.findByOwnerId(mockUser.getId())).thenReturn(existingProjects);

        // Arrange: Prepare a DTO for creating a new project within the project limit
        NewProjectDTO projectDTO = new TestDataFactory.NewProjectDTOBuilder().name("Within Limit Project").build();
        Project newProject = new TestDataFactory.ProjectBuilder().fromDTO(projectDTO).user(mockUser).build();
        when(projectRepository.save(any(Project.class))).thenReturn(newProject);

        // Act: Call the service to create a project within the limit
        Project createdProject = projectService.createProject(projectDTO);

        // Assert: Verify the project was created and saved within the limit
        assertThat(createdProject).isNotNull();
        assertThat(createdProject.getName()).isEqualTo("Within Limit Project");
        verify(projectRepository).save(any(Project.class));

        // Arrange: Add one more project to reach the project limit exactly
        existingProjects.add(createdProject);
        when(projectRepository.findByOwnerId(mockUser.getId())).thenReturn(existingProjects);

        // Act & Assert: Attempting to create another project should throw
        // ProjectLimitReachedException
        NewProjectDTO exceedingProjectDTO = new TestDataFactory.NewProjectDTOBuilder().build();
        assertThrows(ProjectLimitReachedException.class, () -> projectService.createProject(exceedingProjectDTO));
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void updateProject_ShouldUpdateProject_WhenProjectExists() {
        // Arrange: Set up an existing project and mock the repository to return it when
        // searched by ID
        Long resourceId = 1L;
        Project project = new TestDataFactory.ProjectBuilder().name("Old Name").user(mockUser).build();
        when(projectRepository.findById(resourceId)).thenReturn(Optional.of(project));

        // Arrange: Prepare the DTO with the updated project name
        UpdateProjectDTO projectDTO = new TestDataFactory.UpdateProjectDTOBuilder().build();

        // Arrange: Mock the repository save to return the updated project
        when(projectRepository.save(project)).thenReturn(project);

        // Arrange: Mock event logging
        mockLogEvent();

        // Act: Call the service to update the project's name
        projectService.updateProject(resourceId, projectDTO);

        // Capture the saved project to verify fields
        ArgumentCaptor<Project> savedProjectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(savedProjectCaptor.capture());
        Project savedProject = savedProjectCaptor.getValue();

        // Assert: Verify that the project's name was updated as expected
        assertThat(savedProject.getName()).isEqualTo(projectDTO.getName());

        // Capture and verify the arguments passed to logUpdateEvent
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        // Verify the event was logged
        verify(eventService).logEvent(
                eq(ResourceType.PROJECT), eq(resourceId),
                eq(EventChangeType.UPDATE), payloadCaptor.capture());

        // Assert: Verify the payload contains the expected changes
        Map<String, Object> capturedPayload = payloadCaptor.getValue();
        assertThat(capturedPayload)
                .containsEntry("name", savedProject.getName());
    }

    @Test
    void updateProject_ShouldThrowException_WhenProjectDoesNotExist() {
        // Arrange: Mock the repository to return an empty result when searching for a
        // non-existent project ID
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        // Arrange: Prepare the DTO with the updated project name
        UpdateProjectDTO projectDTO = new TestDataFactory.UpdateProjectDTOBuilder().build();

        // Act & Assert: Verify that attempting to update a non-existent project throws
        // ProjectNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(1L, projectDTO));
    }

    @ParameterizedTest
    @CsvSource({
            "true, Project should be deleted when it exists",
            "false, Exception should be thrown when project does not exist"
    })
    void deleteProject_ShouldHandleExistenceCorrectly(boolean projectExists, String description) {
        Long resourceId = 1L;
        if (projectExists) {
            // Arrange: Stub the repository to simulate finding project
            mockProjectInRepository(resourceId);

            // Act: Call the service method
            projectService.deleteProjectById(resourceId);

            // Assert: Verify that the repository's delete method was called with the
            // correct ID
            verify(projectRepository).deleteById(resourceId);
        } else {
            // Arrange: Stub the repository to simulate not finding project
            mockNonexistentProjectInRepository(resourceId);

            // Act & Assert: Attempt to delete the project and expect a
            // ResourceNotFoundException
            assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProjectById(resourceId));

            // Assert: Verify that the repository's delete method was never called
            verify(projectRepository, never()).delete(any(Project.class));
        }
    }

    private void mockNonexistentProjectInRepository(Long resourceId) {
        when(projectRepository.findById(resourceId)).thenReturn(Optional.empty());
    }

    private Project mockProjectInRepository(Long resourceId) {
        Project mockProject = new TestDataFactory.ProjectBuilder().id(resourceId).user(mockUser).build();
        when(projectRepository.findById(resourceId)).thenReturn(Optional.of(mockProject));
        return mockProject;
    }

    private void mockLogEvent() {
        when(eventService.logEvent(any(), anyLong(), any(), any())).thenReturn(new Event());
    }

}
