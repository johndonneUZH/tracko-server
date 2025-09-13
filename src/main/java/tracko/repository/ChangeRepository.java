package tracko.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import tracko.models.change.Change;

import java.util.List;


public interface ChangeRepository extends MongoRepository<Change, String> {
    List<Change> findByProjectId(String projectId);
    List<Change> findByOwnerId(String ownerId);  
    void deleteByProjectId(String projectId);  
    List<Change> findByProjectId(String projectId, String ownerId);
    List<Change> findByOwnerIdAndProjectId(String ownerId, String projectId);
}
