package ch.uzh.ifi.hase.soprafs24.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ch.uzh.ifi.hase.soprafs24.models.Userrr;

public interface Repository extends MongoRepository<Userrr, Long> 
{

}
