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
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.repository.IdeaRepository;

@Service
@Transactional
public class IdeaService {
    
    private final IdeaRepository ideaRepository;
    private final UserService userService;
    private final ProjectService projectService;

    IdeaService(IdeaRepository ideaRepository, UserService userService, ProjectService projectService) {
        this.ideaRepository = ideaRepository;
        this.userService = userService;
        this.projectService = projectService;
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

        return ideaRepository.save(newIdea);
    }

    public List<Idea> getIdeasByProject(String projectId, String authHeader) {
        projectService.authenticateProject(projectId, authHeader);

        // Fetch ideas linked to the provided projectId
        return ideaRepository.findByProjectId(projectId);
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
        ideaRepository.save(idea);

        return idea;
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
        ideaRepository.save(subIdea);
        ideaRepository.save(idea);
    
        return subIdea;
    }
}
