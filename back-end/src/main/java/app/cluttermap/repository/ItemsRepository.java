package app.cluttermap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.Item;

@Repository
public interface ItemsRepository extends CrudRepository<Item, Long> {
    @Query(value = "SELECT i.* FROM items i " +
            "JOIN projects p ON i.project_id = p.id " +
            "WHERE p.owner_id = :ownerId", nativeQuery = true)
    List<Item> findItemsByUserId(@Param("ownerId") Long ownerId);
}
