package app.cluttermap.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import app.cluttermap.model.User;

@Repository
public interface UsersRepository extends CrudRepository<User, Long> {
    Optional <User> findByEmail(String email);
    Optional <User> findByGoogleId(String googleId);
}
