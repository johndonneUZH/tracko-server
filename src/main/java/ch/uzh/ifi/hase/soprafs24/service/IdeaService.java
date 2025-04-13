package ch.uzh.ifi.hase.soprafs24.service;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaRegister;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaUpdate;

import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.repository.CommentRepository;
import ch.uzh.ifi.hase.soprafs24.repository.IdeaRepository;

// WebSocket related

import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@Transactional
public class IdeaService {
    
    private final IdeaRepository ideaRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final ProjectAuthorizationService projectAuthorizationService;
    private final SimpMessagingTemplate messagingTemplate;

    public IdeaService(IdeaRepository ideaRepository, 
                     UserService userService,
                     SimpMessagingTemplate messagingTemplate, 
                     ProjectAuthorizationService projectAuthorizationService,
                     CommentRepository commentRepository) {
        this.ideaRepository = ideaRepository;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
        this.projectAuthorizationService = projectAuthorizationService;
        this.commentRepository = commentRepository;
    }

    public Idea createIdea(String projectId, IdeaRegister inputIdea, String authHeader, ArrayList<String> subIdeas) {
        String userId = userService.getUserIdByToken(authHeader);
    
        Project project = projectAuthorizationService.authenticateProject(projectId, authHeader);
    
        //  creates idea
        Idea newIdea = new Idea();
        newIdea.setOwnerId(userId);
        newIdea.setProjectId(project.getProjectId());  
        newIdea.setIdeaName(inputIdea.getIdeaName());
        newIdea.setIdeaDescription(inputIdea.getIdeaDescription());
        newIdea.setx(inputIdea.getX()); 
        newIdea.sety(inputIdea.gety());  
        newIdea.setUpVotes(new ArrayList<>());  
        newIdea.setDownVotes(new ArrayList<>());  
        newIdea.setComments(subIdeas);
    
        // Saves idea
        newIdea = ideaRepository.save(newIdea);
        messagingTemplate.convertAndSend("/topic/ideas/" + projectId, newIdea);

        return newIdea;
    }
    

    public List<Idea> getIdeasByProject(String projectId, String authHeader) {
        projectAuthorizationService.authenticateProject(projectId, authHeader);

        // Fetch ideas linked to the provided projectId
        return ideaRepository.findByProjectId(projectId);
    }

    public Idea getIdeaById(String projectId, String ideaId, String authHeader) {
        projectAuthorizationService.authenticateProject(projectId, authHeader);

        return ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));
    }

    public Idea updateIdea(String projectId, String ideaId, IdeaUpdate inputIdea, String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);
        Idea idea = ideaRepository.findById(ideaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));
    
        projectAuthorizationService.authenticateProject(projectId, authHeader);
    
        if (!idea.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this idea");
        }
    
        // Only update non-null fields
        if (inputIdea.getIdeaName() != null) {
            idea.setIdeaName(inputIdea.getIdeaName());
        }
        if (inputIdea.getIdeaDescription() != null) {
            idea.setIdeaDescription(inputIdea.getIdeaDescription());
        }
    
        // These are floats, always update (even if 0)
        if (inputIdea.getX() != null) {
            idea.setx(inputIdea.getX());
        }
        if (inputIdea.gety() != null) {
            idea.sety(inputIdea.gety());
        }
        
        // Lists: update only if not null, else keep existing
        if (inputIdea.getUpVotes() != null) {
            idea.setUpVotes(inputIdea.getUpVotes());
        }
        if (inputIdea.getDownVotes() != null) {
            idea.setDownVotes(inputIdea.getDownVotes());
        }
        if (inputIdea.getComments() != null) {
            idea.setComments(inputIdea.getComments());
        }
    
        Idea saved = ideaRepository.save(idea);
        messagingTemplate.convertAndSend("/topic/ideas/" + projectId, saved);
        return saved;
    }
    
    
    public void deleteIdea(String projectId, String ideaId, String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);
        Idea idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));
    
        projectAuthorizationService.authenticateProject(projectId, authHeader);
        
        if (!idea.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this idea");
        }
        
        commentRepository.deleteByIdeaId(ideaId);
        ideaRepository.deleteById(ideaId);
        messagingTemplate.convertAndSend("/topic/ideas/" + projectId, Map.of("deletedId", ideaId));

    }
    
}
