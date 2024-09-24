package app.cluttermap.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.Room;

@Repository
public interface RoomsRepository extends CrudRepository<Room, Long> {
    // We can define custom queries here if needed, but basic CRUD methods are inherited
}

