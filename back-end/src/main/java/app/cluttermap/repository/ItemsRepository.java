package app.cluttermap.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.Item;

@Repository
public interface ItemsRepository extends CrudRepository<Item, Long> {
    // We can define custom queries here if needed, but basic CRUD methods are inherited
}