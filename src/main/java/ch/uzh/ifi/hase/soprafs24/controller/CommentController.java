package ch.uzh.ifi.hase.soprafs24.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs24.models.comment.Comment;
import ch.uzh.ifi.hase.soprafs24.service.CommentService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("/projects/{projectId}/ideas/{ideaId}/comments")
public class CommentController {

    private final CommentService commentService;

    CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<Comment>> getCommentsByIdeaId(
            @PathVariable String projectId,
            @PathVariable String ideaId,
            @RequestHeader("Authorization") String authHeader) {

        List<Comment> comments = commentService.getCommentsByIdea(projectId, ideaId, authHeader);
        return ResponseEntity.status(HttpStatus.OK).body(comments);
    
    }
}
