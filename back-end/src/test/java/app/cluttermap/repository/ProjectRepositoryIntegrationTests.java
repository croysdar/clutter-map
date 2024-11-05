package app.cluttermap.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import app.cluttermap.model.Project;
import app.cluttermap.model.Room;
import app.cluttermap.model.User;
import jakarta.transaction.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProjectRepositoryIntegrationTests {

    @Autowired
    private ProjectsRepository projectRepository;

    @Autowired
    private RoomsRepository roomRepository;

    @Autowired
    private UsersRepository userRepository;

    @Test
    void findByOwner_ShouldReturnOnlyProjectsOwnedBySpecifiedUser() {
        /*
         * Test that findByOwner returns only the projects owned by the specified user.
         * Ensures that the query filters projects based on the owner and does not
         * include projects owned by others.
         */
        User owner1 = new User("owner1ProviderId");
        // created_at,email,first_name,last_name,provider,provider_id,username
        Project project1 = new Project("Project Owned by Owner 1", owner1);

        User owner2 = new User("owner2ProviderId");
        Project project2 = new Project("Project Owned by Owner 2", owner2);

        userRepository.saveAll(List.of(owner1, owner2));
        projectRepository.saveAll(List.of(project1, project2));

        // Find projects by owner1
        List<Project> owner1Projects = projectRepository.findByOwner(owner1);
        assertThat(owner1Projects).hasSize(1);
        assertThat(owner1Projects.get(0).getName()).isEqualTo("Project Owned by Owner 1");

        // Verify that only owner1's project is returned
        assertThat(owner1Projects).doesNotContain(project2);
    }

    @Test
    @Transactional
    void deletingProject_ShouldAlsoDeleteRooms() {
        /*
         * Test that deleting a Project also deletes associated Room entities due to
         * cascading delete.
         * Ensures that removing a Project will not leave orphaned Room records in the
         * database. *
         */
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Test Project", owner);
        Room room = new Room("Living Room", "This is the living room", project);
        project.getRooms().add(room);

        projectRepository.save(project);
        assertThat(roomRepository.findAll()).hasSize(1);

        // Delete the project and check cascading delete
        projectRepository.delete(project);
        assertThat(roomRepository.findAll()).isEmpty(); // Room should be deleted as orphan
    }

    @Test
    void removingRoomFromProject_ShouldTriggerOrphanRemoval() {
        /*
         * Test that removing a Room from a Project's rooms collection triggers
         * orphan removal.
         * Ensures that disassociating a Room from its Project will delete it from
         * the database.
         */
        User owner = new User("ownerProviderId");
        userRepository.save(owner);

        Project project = new Project("Test Project", owner);
        Room room = new Room("Living Room", "This is the living room", project);
        project.getRooms().add(room);

        projectRepository.save(project);
        assertThat(roomRepository.findAll()).hasSize(1); // Room should exist in DB

        // Remove room and save project to trigger orphan removal
        project.getRooms().remove(room);
        projectRepository.save(project);

        assertThat(roomRepository.findAll()).isEmpty(); // Room should be deleted
    }

}
