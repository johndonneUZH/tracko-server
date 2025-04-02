package ch.uzh.ifi.hase.soprafs24.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.models.comment.Comment;
import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;
import ch.uzh.ifi.hase.soprafs24.repository.IdeaRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

@Service
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final IdeaRepository ideaRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, IdeaRepository ideaRepository, UserRepository userRepository, ProjectService projectService) {
        this.projectService = projectService;
        this.commentRepository = commentRepository;
        this.ideaRepository = ideaRepository;
        this.userRepository = userRepository;
    }

    public List<Comment> getCommentsByIdea(String projectId, String ideaId, String authHeader) {
        // Authenticate the project and idea
        projectService.authenticateProject(projectId, authHeader);
        
        Idea idea = ideaRepository.findById(ideaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));
        if (!idea.getProjectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this idea");
        }
        return commentRepository.findByIdeaId(ideaId);
    }
}
