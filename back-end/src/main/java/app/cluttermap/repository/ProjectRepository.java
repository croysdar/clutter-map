package app.cluttermap.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.Project;
import app.cluttermap.model.User;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {
    List<Project> findByOwner(User owner);
}
