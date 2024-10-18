package app.cluttermap.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.OrgUnit;

@Repository
public interface OrgUnitsRepository extends CrudRepository<OrgUnit, Long> {
    // We can define custom queries here if needed, but basic CRUD methods are inherited
}