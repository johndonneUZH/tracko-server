package service;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import constant.ChangeType;
import models.idea.Idea;
import models.idea.IdeaRegister;
import models.idea.IdeaUpdate;
import models.project.Project;
import models.user.User;
import repository.CommentRepository;
import repository.IdeaRepository;

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
    private final ProjectService projectService;
    private final ChangeService changeService; 

    // For sonarqube, so we don't use the same constant multiple times
    private static final String IDEA_NOT_FOUND = "Idea not found";

    public IdeaService(IdeaRepository ideaRepository, 
                     UserService userService,
                     SimpMessagingTemplate messagingTemplate, 
                     ProjectAuthorizationService projectAuthorizationService,
                     CommentRepository commentRepository,
                     ProjectService projectService,
                     ChangeService changeService) {
        this.changeService = changeService;
        this.ideaRepository = ideaRepository;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
        this.projectAuthorizationService = projectAuthorizationService;
        this.commentRepository = commentRepository;
        this.projectService = projectService; 
    }

    private void broadcastIdeaUpdate(String projectId, Idea idea) {
        messagingTemplate.convertAndSend(
            "/topic/projects/" + projectId + "/ideas", 
            idea
        );
    }

    private void broadcastIdeaDeletion(String projectId, String ideaId) {
        messagingTemplate.convertAndSend(
            "/topic/projects/" + projectId + "/ideas",
            Map.of("deletedId", ideaId)
        );
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
        broadcastIdeaUpdate(projectId, newIdea);
        changeService.markChange(projectId, ChangeType.ADDED_IDEA, authHeader, false, null);
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, IDEA_NOT_FOUND));
    }

    public Idea updateIdea(String projectId, String ideaId, IdeaUpdate inputIdea, String authHeader) {
        Idea idea = ideaRepository.findById(ideaId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, IDEA_NOT_FOUND));
    
        Project project = projectAuthorizationService.authenticateProject(projectId, authHeader);

        User user = userService.getUserByToken(authHeader);
    
        boolean actualChange = false;

        // Only update non-null fields
        if (inputIdea.getIdeaName() != null) {
            idea.setIdeaName(inputIdea.getIdeaName());
            actualChange = true;
        }
        if (inputIdea.getIdeaDescription() != null) {
            idea.setIdeaDescription(inputIdea.getIdeaDescription());
            actualChange = true;
        }

        if (actualChange && !idea.getOwnerId().equals(user.getId()) && !project.getOwnerId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this idea");
        }
    
        // These are floats, always update (even if 0)
        if (inputIdea.getX() != null) {
            idea.setx(inputIdea.getX());
        }
        if (inputIdea.gety() != null) {
            idea.sety(inputIdea.gety());
        }

        boolean upVote = false;
        boolean downVote = false;
        
        // Lists: update only if not null, else keep existing
        if (inputIdea.getUpVotes() != null) {
            idea.setUpVotes(inputIdea.getUpVotes());
            upVote = true;
        }
        if (inputIdea.getDownVotes() != null) {
            idea.setDownVotes(inputIdea.getDownVotes());
            downVote = true;
        }
        if (inputIdea.getComments() != null) {
            idea.setComments(inputIdea.getComments());
        }
    
        Idea saved = ideaRepository.save(idea);
        broadcastIdeaUpdate(projectId, saved);
        if (actualChange) {
            changeService.markChange(projectId, ChangeType.MODIFIED_IDEA, authHeader, false, null);
        } else if (upVote) {
            changeService.markChange(projectId, ChangeType.UPVOTE, authHeader, false, null);
        } else if (downVote) {
            changeService.markChange(projectId, ChangeType.DOWNVOTE, authHeader, false, null);
        }
        return saved;
    }
    
    
    public void deleteIdea(String projectId, String ideaId, String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);
        String ownerId = projectService.getOwnerIdByProjectId(projectId);
        
        Idea idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, IDEA_NOT_FOUND));

        Project project = projectAuthorizationService.authenticateProject(projectId, authHeader);
    
        if (!idea.getOwnerId().equals(userId) && !userId.equals(ownerId) && !project.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this idea");
        }

        commentRepository.deleteByIdeaId(ideaId);
        ideaRepository.deleteById(ideaId);
        broadcastIdeaDeletion(projectId, ideaId);
        changeService.markChange(projectId, ChangeType.CLOSED_IDEA, authHeader, false, null);

    }
    
}
