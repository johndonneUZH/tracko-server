package repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import models.user.User;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> 
{
    User findByUsername(String username);
    Optional<User> findById(String id);
    User findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
