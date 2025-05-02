package ch.uzh.ifi.hase.soprafs24.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import ch.uzh.ifi.hase.soprafs24.models.messages.Message;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByProjectId(String projectId);
    void deleteByProjectId(String projectId);
    List<Message> findByProjectIdOrderByCreatedAtAsc(String projectId);
}
