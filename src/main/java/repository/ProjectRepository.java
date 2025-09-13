package repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import models.project.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {
    Project findByProjectName(String projectName);
    Optional<Project> findById(String id);
    boolean existsByProjectName(String projectName);
    boolean existsById(String id);
    List<Project> findByOwnerId(String ownerId);
    List<Project> findByProjectMembers(String userId);
    void deleteById(String id);
    Project findByOwnerIdAndProjectName(String ownerId, String projectName);
}