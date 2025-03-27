package ch.uzh.ifi.hase.soprafs24.service;

import java.util.ArrayList;

import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

import ch.uzh.ifi.hase.soprafs24.constant.IdeaStatus;
import ch.uzh.ifi.hase.soprafs24.models.idea.Idea;
import ch.uzh.ifi.hase.soprafs24.models.idea.IdeaRegister;
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

    public Idea createIdea(IdeaRegister inputIdea, String authHeader) {

        String userId = userService.getUserIdByToken(authHeader);
        Project project = projectService.authenticateProject(inputIdea.getProjectId(), authHeader);
        
        Idea newIdea = new Idea();
        newIdea.setOwnerId(userId);
        newIdea.setProjectId(project.getProjectId());
        newIdea.setIdeaName(inputIdea.getIdeaName());
        newIdea.setIdeaDescription(inputIdea.getIdeaDescription());
        newIdea.setCreatedAt(java.time.LocalDateTime.now());
        newIdea.setUpdatedAt(java.time.LocalDateTime.now());
        newIdea.setIdeaStatus(IdeaStatus.OPEN);
        newIdea.setUpVotes(0L);
        newIdea.setDownVotes(0L);
        newIdea.setSubIdeas(new ArrayList<>());
        return ideaRepository.save(newIdea);
    }
}
