package tracko.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tracko.models.comment.Comment;
import tracko.models.comment.CommentRegister;
import tracko.service.CommentService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("/projects/{projectId}/ideas/{ideaId}/comments")
public class CommentController {

    private final CommentService commentService;

    CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("")
    public ResponseEntity<List<Comment>> getCommentsByIdeaId(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @RequestHeader("Authorization") String authHeader) {

        List<Comment> comments = commentService.getCommentsByIdea(projectId, ideaId, authHeader);
        return ResponseEntity.status(HttpStatus.OK).body(comments);
    
    }

    @PostMapping("")
    public ResponseEntity<Comment> createRootComment(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CommentRegister comment) {

        Comment createdComment = commentService.createComment(projectId, ideaId, null, authHeader, comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PostMapping("/{commentId}")
    public ResponseEntity<Comment> createComment(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @PathVariable String commentId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CommentRegister comment) {

        Comment createdComment = commentService.createComment(projectId, ideaId, commentId, authHeader, comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<Comment> getCommentById(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @PathVariable String commentId,
            @RequestHeader("Authorization") String authHeader) {
    
        Comment comment = commentService.getCommentById(projectId, ideaId, commentId, authHeader);
        return ResponseEntity.status(HttpStatus.OK).body(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @PathVariable String commentId,
            @RequestHeader("Authorization") String authHeader) {
    
        commentService.deleteComment(commentId, authHeader);
        return ResponseEntity.noContent().build();
    }
    

}
