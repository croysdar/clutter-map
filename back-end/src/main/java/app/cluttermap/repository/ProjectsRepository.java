package app.cluttermap.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.Project;

@Repository
public interface ProjectsRepository extends CrudRepository<Project, Long> {
    
}
