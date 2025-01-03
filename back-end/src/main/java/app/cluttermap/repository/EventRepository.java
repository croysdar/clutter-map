package app.cluttermap.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.Event;
import app.cluttermap.model.Project;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {
    // This gives you a timeline for the project
    @EntityGraph(attributePaths = { "user.id", "user.username" })
    @Query("SELECT e FROM Event e WHERE e.project = :project ORDER BY e.timestamp DESC")
    Page<Event> findAllEventsInProject(Project project, Pageable pageable);

}