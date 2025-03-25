package ch.uzh.ifi.hase.soprafs24.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ch.uzh.ifi.hase.soprafs24.models.UserRegister;
import java.util.Optional;

public interface Repository extends MongoRepository<UserRegister, String> 
{
    UserRegister findByUsername(String username);
    Optional<UserRegister> findById(String id);
    UserRegister findByEmail(String email);
}
