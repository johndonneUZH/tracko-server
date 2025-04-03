package ch.uzh.ifi.hase.soprafs24.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ch.uzh.ifi.hase.soprafs24.models.comment.Comment;
import ch.uzh.ifi.hase.soprafs24.models.comment.CommentRegister;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final IdeaService ideaService;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, IdeaService ideaService, UserService userService) {
        this.commentRepository = commentRepository;
        this.ideaService = ideaService;
        this.userService = userService;
    }

    public List<Comment> getCommentsByIdea(String projectId, String ideaId, String authHeader) {
        // Authenticate the project and idea
        ideaService.getIdeaById(projectId, ideaId, authHeader);

        // Return comments linked to the provided ideaId
        return commentRepository.findByIdeaId(ideaId);
    }

    public Comment createComment(String projectId, String ideaId, String authHeader, CommentRegister comment) {
        // Authenticate the project and idea
        ideaService.getIdeaById(projectId, ideaId, authHeader);
        String userId = userService.getUserIdByToken(authHeader);

        Comment newComment = new Comment();
        newComment.setCommentText(comment.getCommentText());
        newComment.setIdeaId(ideaId);
        newComment.setOwnerId(userId);
        newComment.setProjectId(projectId);
        newComment.setCreatedAt(java.time.LocalDateTime.now());

        if (comment.getParentId() != null) {
            newComment.setParentCommentId(comment.getParentId());
        } else {
            newComment.setParentCommentId(null);
        }

        return commentRepository.save(newComment);
    }
}
