package service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import repository.CommentRepository;
import constant.ChangeType;
import models.comment.Comment;
import models.comment.CommentRegister;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    // private final IdeaService ideaService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private IdeaService ideaService; // No final
    private final ChangeService changesService;

    @Autowired
    public void setIdeaService(IdeaService ideaService) {
        this.ideaService = ideaService;
    }

    public CommentService(  CommentRepository commentRepository, 
                            IdeaService ideaService, 
                            UserService userService, 
                            SimpMessagingTemplate messagingTemplate,
                            ChangeService changesService) {
        this.changesService = changesService;
        this.commentRepository = commentRepository;
        // this.ideaService = ideaService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Comment> getCommentsByIdea(String projectId, String ideaId, String authHeader) {
        // Authenticate the project and idea
        ideaService.getIdeaById(projectId, ideaId, authHeader);

        // Return comments linked to the provided ideaId
        return commentRepository.findByIdeaId(ideaId);
    }

    public List<Comment> getCommentsByIdeaId(String ideaId) {
        return commentRepository.findByIdeaId(ideaId);
    }

public Comment createComment(String projectId, String ideaId, String parentCommentId, String authHeader, CommentRegister comment) {
    // Authenticate project and idea
    ideaService.getIdeaById(projectId, ideaId, authHeader);
    String userId = userService.getUserIdByToken(authHeader);

    // Create new comment
    Comment newComment = new Comment();
    newComment.setCommentText(comment.getCommentText());
    newComment.setIdeaId(ideaId);
    newComment.setOwnerId(userId);
    newComment.setReplies(new ArrayList<>()); // initialize empty replies list
    newComment.setProjectId(projectId);
    newComment.setCreatedAt(LocalDateTime.now()); // Set creation timestamp

    // Save comment to get the new ID
    Comment savedComment = commentRepository.save(newComment);

    // If this is a reply, attach it to parent
    if (parentCommentId != null) {
        Comment parent = commentRepository.findById(parentCommentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found"));
        List<String> replies = parent.getReplies();
        replies.add(savedComment.getCommentId());
        parent.setReplies(replies);
        commentRepository.save(parent);
    }

    // WebSocket payload: include comment and parentId (if any)
    Map<String, Object> payload = new HashMap<>();
    payload.put("comment", savedComment);
    if (parentCommentId != null) {
        payload.put("parentId", parentCommentId);
    }

    messagingTemplate.convertAndSend("/topic/comments/" + ideaId, payload);

    // Broadcast the change
    changesService.markChange(projectId, ChangeType.ADDED_COMMENT, authHeader, false, null);

    return savedComment;
}

    public Comment getCommentById(String projectId, String ideaId, String commentId, String authHeader) {
    ideaService.getIdeaById(projectId, ideaId, authHeader);

    return commentRepository.findById(commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
}

    public void deleteComment(String commentId, String authHeader) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        String userId = userService.getUserIdByToken(authHeader);
        if (!comment.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this comment");
        }
        deleteReplies(commentId, comment, authHeader);
        commentRepository.delete(comment);
        messagingTemplate.convertAndSend("/topic/comments/" + comment.getIdeaId(), Map.of("deletedId", comment.getCommentId()));


        changesService.markChange(comment.getProjectId(), ChangeType.DELETED_COMMENT, authHeader, false, null);
    }   

    private void deleteReplies(String commentId, Comment comment, String authHeader) {
        List<String> replies = comment.getReplies();
        for (String replyId : replies) {
            commentRepository.deleteById(replyId);
        }
    }

}
