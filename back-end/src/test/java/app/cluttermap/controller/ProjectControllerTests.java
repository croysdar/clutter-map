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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
    void getProjects_ShouldReturnAllUserProjects() throws Exception {
        Project project = new Project("Test Project", mockUser);
        when(projectService.getUserProjects()).thenReturn(Collections.singletonList(project));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Project"));

        verify(projectService).getUserProjects();
    }

    @Test
    void getProjects_ShouldReturnOnlyCurrentUserProjects() throws Exception {
        User anotherUser = new User("otherProviderId");
        Project userProject = new Project("Test Project", mockUser);
        Project otherUserProject = new Project("Other User's Project", anotherUser);

        when(projectService.getUserProjects()).thenReturn(Collections.singletonList(userProject));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Project"))
                .andExpect(jsonPath("$.length()").value(1)); // Ensure only one project is returned

        verify(projectService).getUserProjects();
    }

    @Test
    void getOneProject_ShouldReturnProject_WhenProjectExists() throws Exception {
        Project project = new Project("Test Project", mockUser);
        when(projectService.getProjectById(1L)).thenReturn(project);

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Project"));

        verify(projectService).getProjectById(1L);
    }

    @Test
    void getOneProject_ShouldReturnNotFound_WhenProjectDoesNotExist() throws Exception {
        when(projectService.getProjectById(1L)).thenThrow(new ProjectNotFoundException());

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isNotFound());

        verify(projectService).getProjectById(1L);
    }

    @Test
    void addOneProject_ShouldCreateProject_WhenValidRequest() throws Exception {
        NewProjectDTO projectDTO = new NewProjectDTO("New Project");
        Project newProject = new Project("New Project", mockUser);
        when(projectService.createProject(any(NewProjectDTO.class))).thenReturn(newProject);

        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Project"));

        verify(projectService).createProject(any(NewProjectDTO.class));
    }

    @Test
    void addOneProject_ShouldReturnBadRequest_WhenProjectNameIsBlank() throws Exception {
        NewProjectDTO projectDTO = new NewProjectDTO("");

        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void addOneProject_ShouldReturnBadRequest_WhenProjectNameIsNull() throws Exception {
        NewProjectDTO projectDTO = new NewProjectDTO(null);

        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void updateOneProject_ShouldUpdateProject_WhenValidRequest() throws Exception {
        UpdateProjectDTO projectDTO = new UpdateProjectDTO("Updated Project");
        Project updatedProject = new Project("Updated Project", mockUser);
        when(projectService.updateProject(eq(1L), any(UpdateProjectDTO.class))).thenReturn(updatedProject);

        mockMvc.perform(put("/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Project"));

        verify(projectService).updateProject(eq(1L), any(UpdateProjectDTO.class));
    }

    @Test
    void updateOneProject_ShouldReturnBadRequest_WhenProjectNameIsBlank() throws Exception {
        UpdateProjectDTO projectDTO = new UpdateProjectDTO(""); // Blank project name

        mockMvc.perform(put("/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void updateOneProject_ShouldReturnBadRequest_WhenProjectNameIsNull() throws Exception {
        UpdateProjectDTO projectDTO = new UpdateProjectDTO(null);

        mockMvc.perform(put("/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Project name must not be blank."));
    }

    @Test
    void deleteOneProject_ShouldDeleteProject_WhenProjectExists() throws Exception {
        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isNoContent());

        verify(projectService).deleteProject(1L);
    }

    @Test
    void deleteOneProject_ShouldReturnNotFound_WhenProjectDoesNotExist() throws Exception {
        doThrow(new ProjectNotFoundException()).when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isNotFound());

        verify(projectService).deleteProject(1L);
    }

    @Test
    void getProjectRooms_ShouldReturnRooms_WhenProjectExists() throws Exception {
        Project project = new Project("Test Project", mockUser);
        Room room = new Room("Living Room", "This is the living room", project);
        project.setRooms(Collections.singletonList(room));
        when(projectService.getProjectById(1L)).thenReturn(project);

        mockMvc.perform(get("/projects/1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Living Room"));

        verify(projectService).getProjectById(1L);
    }

    @Test
    void getProjectRooms_ShouldReturnNotFound_WhenProjectDoesNotExist() throws Exception {
        when(projectService.getProjectById(1L)).thenThrow(new ProjectNotFoundException());

        mockMvc.perform(get("/projects/1/rooms"))
                .andExpect(status().isNotFound());

        verify(projectService).getProjectById(1L);
    }
}
