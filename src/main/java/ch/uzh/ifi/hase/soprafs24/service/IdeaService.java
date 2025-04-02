package ch.uzh.ifi.hase.soprafs24.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.IdeaStatus;
import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaRegister;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaUpdate;
import ch.uzh.ifi.hase.soprafs24.models.websocket.IdeaUpdateMessage;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.repository.IdeaRepository;

// WebSocket related
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@Transactional
public class IdeaService {
    
    private final IdeaRepository ideaRepository;
    private final UserService userService;
    private final ProjectService projectService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    IdeaService(IdeaRepository ideaRepository, UserService userService, ProjectService projectService, SimpMessagingTemplate messagingTemplate) {
        this.ideaRepository = ideaRepository;
        this.userService = userService;
        this.projectService = projectService;
        this.messagingTemplate = messagingTemplate;
    }

    public Idea createIdea(String projectId, IdeaRegister inputIdea, String authHeader, ArrayList<String> subIdeas) {
        String userId = userService.getUserIdByToken(authHeader);

        // Authenticate project association
        Project project = projectService.authenticateProject(projectId, authHeader);

        // Create the idea object
        Idea newIdea = new Idea();
        newIdea.setOwnerId(userId);
        newIdea.setProjectId(project.getProjectId());  // Use the path variable projectId
        newIdea.setIdeaName(inputIdea.getIdeaName());
        newIdea.setIdeaDescription(inputIdea.getIdeaDescription());
        newIdea.setCreatedAt(java.time.LocalDateTime.now());
        newIdea.setUpdatedAt(java.time.LocalDateTime.now());
        newIdea.setIdeaStatus(IdeaStatus.OPEN);
        newIdea.setUpVotes(0L);
        newIdea.setDownVotes(0L);
        newIdea.setSubIdeas(subIdeas);

        // Save the new idea
        newIdea = ideaRepository.save(newIdea);
    
        // Broadcast the new idea to all subscribers
        IdeaUpdateMessage message = new IdeaUpdateMessage();
        message.setAction("CREATE");
        message.setIdeaId(newIdea.getIdeaId());
        message.setProjectId(projectId);
        message.setIdea(newIdea);
        
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/ideas", message);

        return newIdea;
    }

    public List<Idea> getIdeasByProject(String projectId, String authHeader) {
        projectService.authenticateProject(projectId, authHeader);

        // Fetch ideas linked to the provided projectId
        return ideaRepository.findByProjectId(projectId);
    }

    public Idea getIdeaById(String projectId, String ideaId, String authHeader) {
        projectService.authenticateProject(projectId, authHeader);

        return ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));
    }

    public Idea updateIdea(String projectId, String ideaId, IdeaUpdate inputIdea, String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);
        Idea idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));

        projectService.authenticateProject(projectId, authHeader);
        
        // Authenticate user association
        if (!idea.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this idea");
        }

        // Update the idea object
        idea.setIdeaName(inputIdea.getIdeaName());
        idea.setIdeaDescription(inputIdea.getIdeaDescription());
        idea.setUpdatedAt(java.time.LocalDateTime.now());
        idea.setIdeaStatus(inputIdea.getIdeaStatus());
        idea.setUpVotes(inputIdea.getUpVotes());
        idea.setDownVotes(inputIdea.getDownVotes());

        List<String> inputSubIdeas = inputIdea.getSubIdeas();
        List<String> currentSubIdeas = idea.getSubIdeas();

        // Merge the two lists without duplicates
        Set<String> mergedSubIdeas = new HashSet<>(currentSubIdeas);
        mergedSubIdeas.addAll(inputSubIdeas);

        // Convert the set back to a list and update the idea
        idea.setSubIdeas(new ArrayList<>(mergedSubIdeas));
        Idea updatedIdea = ideaRepository.save(idea);

        // Broadcast the update to all subscribers
        IdeaUpdateMessage message = new IdeaUpdateMessage();
        message.setAction("UPDATE");
        message.setIdeaId(updatedIdea.getIdeaId());
        message.setProjectId(projectId);
        message.setIdea(updatedIdea);
        
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/ideas", message);

        return updatedIdea;
    }

    public Idea createSubIdea(String projectId, String ideaId, IdeaRegister newIdea, String authHeader) {
        Idea idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));

        // Authenticate project association
        projectService.authenticateProject(projectId, authHeader);

        Idea subIdea = createIdea(projectId, newIdea, authHeader, new ArrayList<>());

        List<String> currentSubIdeas = idea.getSubIdeas();
        Set<String> mergedSubIdeas = new HashSet<>(currentSubIdeas); // Use a Set to avoid duplicates
        mergedSubIdeas.add(subIdea.getIdeaId()); // Add the new sub-idea ID
        idea.setSubIdeas(new ArrayList<>(mergedSubIdeas)); // Convert back to a List
    
        // Save both the parent idea and the sub-idea
        ideaRepository.save(idea);

        // Broadcast the parent idea update to all subscribers
        IdeaUpdateMessage parentMessage = new IdeaUpdateMessage();
        parentMessage.setAction("UPDATE");
        parentMessage.setIdeaId(idea.getIdeaId());
        parentMessage.setProjectId(projectId);
        parentMessage.setIdea(idea);

        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/ideas", parentMessage);

        ideaRepository.save(subIdea);
    
        return subIdea;
    }

    public void deleteIdea(String projectId, String ideaId, String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);
        Idea idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Idea not found"));
    
        projectService.authenticateProject(projectId, authHeader);
        
        // Authenticate user association
        if (!idea.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this idea");
        }
    
        // Delete the idea
        ideaRepository.deleteById(ideaId);
    
        // Broadcast the deletion to all subscribers
        IdeaUpdateMessage message = new IdeaUpdateMessage();
        message.setAction("DELETE");
        message.setIdeaId(ideaId);
        message.setProjectId(projectId);
        message.setIdea(idea); // Including the full idea for reference before deletion
        
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/ideas", message);
    }
}
