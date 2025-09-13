package repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

import models.idea.Idea;

public interface IdeaRepository extends MongoRepository<Idea, String> {
    List<Idea> findByOwnerId(String ownerId);
    List<Idea> findByProjectId(String projectId);
    void deleteByProjectId(String projectId);
}


