package ch.uzh.ifi.hase.soprafs24.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ch.uzh.ifi.hase.soprafs24.models.UserRegister;
import ch.uzh.ifi.hase.soprafs24.models.UserGet;

public interface Repository extends MongoRepository<UserRegister, Long> 
{
    UserGet findByUsername(String username);
    UserGet findById(String id);
}
