package ch.uzh.ifi.hase.soprafs24.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
import ch.uzh.ifi.hase.soprafs24.models.user.User;

public interface IdeaRepository extends MongoRepository<Idea, String> {
    List<User> findByOwnerId(String ownerId);
    List<User> findByProjectId(String projectId);
}
