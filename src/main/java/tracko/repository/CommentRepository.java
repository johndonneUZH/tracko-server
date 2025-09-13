package tracko.repository;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import tracko.models.comment.Comment;


public interface CommentRepository extends MongoRepository<Comment, String> {
    //List<Comment> findByParentCommentId(String parentCommentId);
    List<Comment> findByIdeaId(String ideaId);
    List<Comment> findByOwnerId(String ownerId);
    void deleteByIdeaId(String ideaId);


}