package app.cluttermap.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.cluttermap.exception.project.ProjectNotFoundException;
import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import app.cluttermap.model.dto.NewProjectDTO;
import app.cluttermap.model.dto.UpdateProjectDTO;
import app.cluttermap.service.ProjectService;
import app.cluttermap.service.SecurityService;

@WebMvcTest(ProjectsController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class ProjectControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private SecurityService securityService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User("mockProviderId");
        when(securityService.getCurrentUser()).thenReturn(mockUser);
        // Stub isResourceOwner to allow access to protected resources
        when(securityService.isResourceOwner(anyLong(), eq("project"))).thenReturn(true);
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getUserProjects_ShouldReturnAllUserProjects() throws Exception {
        // Arrange: Set up mock user projects and mock the service to return them
        Project project1 = new Project("Test Project 1", mockUser);
        Project project2 = new Project("Test Project 2", mockUser);
        when(projectService.getUserProjects()).thenReturn(List.of(project1, project2));

        // Act: Perform a GET request to the /projects endpoint
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected project names
                .andExpect(jsonPath("$[0].name").value("Test Project 1"))
                .andExpect(jsonPath("$[1].name").value("Test Project 2"));

        // Assert: Ensure that the service method was called
        verify(projectService).getUserProjects();
    }

    @Test
    void getUserProjects_ShouldReturnEmptyList_WhenNoProjectsExist() throws Exception {
        // Arrange: Set up the service to return an empty list
        when(projectService.getUserProjects()).thenReturn(Collections.emptyList());

        // Act: Perform a GET request to the /projects endpoint
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains an empty array
                .andExpect(jsonPath("$").isEmpty());

        // Assert: Ensure that the service method was called
        verify(projectService).getUserProjects();
    }

    @Test
    void getOneProject_ShouldReturnProject_WhenProjectExists() throws Exception {
        // Arrange: Set up a mock project and stub the service to return it when
        // searched by ID
        Project project = new Project("Test Project", mockUser);
        when(projectService.getProjectById(1L)).thenReturn(project);

        // Act: Perform a GET request to the /projects/1 endpoint
        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                // Assert: Verify the response contains the expected project name
                .andExpect(jsonPath("$.name").value("Test Project"));

        // Assert: Ensure that the service method was called
        verify(projectService).getProjectById(1L);
    }

    @Test
    void getOneProject_ShouldReturnNotFound_WhenProjectDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw ProjectNotFoundException when a
        // non-existent project ID is requested
        when(projectService.getProjectById(1L)).thenThrow(new ProjectNotFoundException());

        // Act: Perform a GET request to the /projects/1 endpoint
        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isNotFound());

        // Assert: Ensure that the service method was called
        verify(projectService).getProjectById(1L);
    }

    @Test
    void addOneProject_ShouldCreateProject_WhenValidRequest() throws Exception {
        // Arrange: Set up a NewProjectDTO with valid data and mock the service to
        // return a new project
        NewProjectDTO projectDTO = new NewProjectDTO("New Project");
        Project newProject = new Project("New Project", mockUser);
        when(projectService.createProject(any(NewProjectDTO.class))).thenReturn(newProject);

        // Act: Perform a POST request to the /projects endpoint with the project data
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected project name
                .andExpect(jsonPath("$.name").value("New Project"));

        // Assert: Ensure the service method was called to create the project
        verify(projectService).createProject(any(NewProjectDTO.class));
    }

    @Test
    void addOneProject_ShouldReturnBadRequest_WhenProjectNameIsBlank() throws Exception {
        // Arrange: Set up a NewProjectDTO with a blank name to trigger validation
        NewProjectDTO projectDTO = new NewProjectDTO("");

        // Act: Perform a POST request to the /projects endpoint with the blank project
        // name
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void addOneProject_ShouldReturnBadRequest_WhenProjectNameIsNull() throws Exception {
        // Arrange: Set up a NewProjectDTO with a null name to trigger validation
        NewProjectDTO projectDTO = new NewProjectDTO(null);

        // Act: Perform a POST request to the /projects endpoint with the null project
        // name
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void updateOneProject_ShouldUpdateProject_WhenValidRequest() throws Exception {
        // Arrange: Set up an UpdateProjectDTO with a new name and mock the service to
        // return the updated project
        UpdateProjectDTO projectDTO = new UpdateProjectDTO("Updated Project");
        Project updatedProject = new Project("Updated Project", mockUser);
        when(projectService.updateProject(eq(1L), any(UpdateProjectDTO.class))).thenReturn(updatedProject);

        // Act: Perform a PUT request to the /projects/1 endpoint with the update data
        mockMvc.perform(put("/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the updated project name
                .andExpect(jsonPath("$.name").value("Updated Project"));

        // Assert: Ensure the service method was called
        verify(projectService).updateProject(eq(1L), any(UpdateProjectDTO.class));
    }

    @Test
    void updateOneProject_ShouldReturnBadRequest_WhenProjectNameIsBlank() throws Exception {
        // Arrange: Set up an UpdateProjectDTO with a blank project name to trigger
        // validation
        UpdateProjectDTO projectDTO = new UpdateProjectDTO("");

        // Act: Perform a PUT request to the /projects/1 endpoint with the invalid
        // project name
        mockMvc.perform(put("/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void updateOneProject_ShouldReturnBadRequest_WhenProjectNameIsNull() throws Exception {
        // Arrange: Set up an UpdateProjectDTO with a null project name to trigger
        // validation
        UpdateProjectDTO projectDTO = new UpdateProjectDTO(null);

        // Act: Perform a PUT request to the /projects/1 endpoint with the null project
        // name
        mockMvc.perform(put("/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())

                // Assert: Verify the validation error response for the name field
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void deleteOneProject_ShouldDeleteProject_WhenProjectExists() throws Exception {
        // Act: Perform a DELETE request to the /projects/1 endpoint
        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isNoContent());

        // Assert: Ensure the service method was called to delete the project by ID
        verify(projectService).deleteProject(1L);
    }

    @Test
    void deleteOneProject_ShouldReturnNotFound_WhenProjectDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw ProjectNotFoundException when deleting a
        // non-existent project
        doThrow(new ProjectNotFoundException()).when(projectService).deleteProject(1L);

        // Act: Perform a DELETE request to the /projects/1 endpoint
        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isNotFound());

        // Assert: Ensure the service method was called to attempt to delete the project
        verify(projectService).deleteProject(1L);
    }

    @Test
    void getProjectRooms_ShouldReturnRooms_WhenProjectExists() throws Exception {
        // Arrange: Set up a project with a room and mock the service to return the
        // project
        Project project = new Project("Test Project", mockUser);
        Room room = new Room("Living Room", "This is the living room", project);
        project.setRooms(Collections.singletonList(room));
        when(projectService.getProjectById(1L)).thenReturn(project);

        // Act: Perform a GET request to the /projects/1/rooms endpoint
        mockMvc.perform(get("/projects/1/rooms"))
                .andExpect(status().isOk())

                // Assert: Verify the response contains the expected room name
                .andExpect(jsonPath("$[0].name").value("Living Room"));

        // Assert: Ensure the service method was called to retrieve the project by ID
        verify(projectService).getProjectById(1L);
    }

    @Test
    void getProjectRooms_ShouldReturnNotFound_WhenProjectDoesNotExist() throws Exception {
        // Arrange: Mock the service to throw ProjectNotFoundException when retrieving
        // rooms for a non-existent project
        when(projectService.getProjectById(1L)).thenThrow(new ProjectNotFoundException());

        // Act: Perform a GET request to the /projects/1/rooms endpoint
        mockMvc.perform(get("/projects/1/rooms"))
                .andExpect(status().isNotFound());

        // Assert: Ensure the service method was called to attempt to retrieve the
        // project
        verify(projectService).getProjectById(1L);
    }
}
