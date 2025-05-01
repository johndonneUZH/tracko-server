package ch.uzh.ifi.hase.soprafs24.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.models.messages.Message;
import ch.uzh.ifi.hase.soprafs24.models.messages.MessageRegister;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import ch.uzh.ifi.hase.soprafs24.models.project.ProjectRegister;
import ch.uzh.ifi.hase.soprafs24.models.project.ProjectUpdate;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.repository.IdeaRepository;
import ch.uzh.ifi.hase.soprafs24.repository.MessageRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ProjectRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

@Service
@Transactional
public class ProjectService {

    private final IdeaRepository ideaRepository;

    
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final ChangeService changeService;
    private final ProjectAuthorizationService projectAuthorizationService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, JwtUtil jwtUtil, 
                          UserService userService, ChangeService changeService, 
                          ProjectAuthorizationService projectAuthorizationService, IdeaRepository ideaRepository,
                          MessageRepository messageRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.projectAuthorizationService = projectAuthorizationService;
        this.changeService = changeService;
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.ideaRepository = ideaRepository;
        this.userRepository = userRepository;
    }

    public Project createProject(ProjectRegister inputProject, String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);
        Project existingProject = projectRepository.findByOwnerIdAndProjectName(userId, inputProject.getProjectName());
        if (existingProject != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project with this name already exists");
        }        

        Project newProject = new Project();

        newProject.setOwnerId(userId);
        newProject.setProjectName(inputProject.getProjectName());
        newProject.setProjectDescription(inputProject.getProjectDescription());
        newProject.setProjectMembers(inputProject.getProjectMembers() != null ? inputProject.getProjectMembers() : new ArrayList<>());
        newProject.setCreatedAt(java.time.LocalDateTime.now());
        newProject.setUpdatedAt(java.time.LocalDateTime.now());
        newProject.setProjectLogoUrl(inputProject.getProjectLogoUrl() != null ? inputProject.getProjectLogoUrl() : null);
        
        Project savedProject = projectRepository.save(newProject);
        userService.addProjectIdToUser(userId, savedProject.getProjectId());
        if (inputProject.getProjectMembers() != null) {
            for (String memberId : inputProject.getProjectMembers()) {
                userService.addProjectIdToUser(memberId, savedProject.getProjectId());
            }
        }

        return savedProject;
    }

    public List<Project> getProjectsByUserId(String userId) {
        List<Project> projectOwned = projectRepository.findByOwnerId(userId);
        List<Project> projectMember = projectRepository.findByProjectMembers(userId);
        Set<Project> allProjects = new HashSet<>(projectOwned);
        allProjects.addAll(projectMember);
        return new ArrayList<>(allProjects);

    }

    public Project updateProject(String projectId, ProjectUpdate updatedProject, String authHeader) {
        Project project = projectAuthorizationService.authenticateProject(projectId, authHeader);

        String userId = userService.getUserIdByToken(authHeader);
        if (!project.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this project");
        }

        if (updatedProject.getProjectName() != null) {
            project.setProjectName(updatedProject.getProjectName());
        }
        if (updatedProject.getProjectDescription() != null) {
            project.setProjectDescription(updatedProject.getProjectDescription());
        }
        if (updatedProject.getProjectLogoUrl() != null) {
            project.setProjectLogoUrl(updatedProject.getProjectLogoUrl());
        }
    
        project.setUpdatedAt(java.time.LocalDateTime.now());

        // Members logic
        HashSet<String> members = new HashSet<>(project.getProjectMembers());

        if (updatedProject.getMembersToAdd() != null) {
            members.addAll(updatedProject.getMembersToAdd());
            for (String memberId : updatedProject.getMembersToAdd()) {
                userService.addProjectIdToUser(memberId, project.getProjectId());
            }
        }
        
        if (updatedProject.getMembersToRemove() != null) {
            members.removeAll(updatedProject.getMembersToRemove());
            for (String memberId : updatedProject.getMembersToRemove()) {
                userService.deleteProjectFromUser(memberId, project.getProjectId());
            }

        }
        
        project.setProjectMembers(new ArrayList<>(members));
        
        projectRepository.save(project);
        return project;

    }

    public void deleteProject(String projectId, String authHeader) {
        Project project = projectAuthorizationService.authenticateProject(projectId, authHeader);

        String userId = userService.getUserIdByToken(authHeader);
        if (!project.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this project");
        }

        // Remove the project from all members
        for (String memberId : project.getProjectMembers()) {
            User projectmember = userService.getUserById(memberId);
            userService.deleteProjectFromUser(projectmember.getId(), projectId);
        }
        deleteProjectChanges(projectId, authHeader);
        deleteProjectIdeas(projectId, authHeader);
        userService.deleteProjectFromUser(userId, projectId);
        projectRepository.deleteById(project.getProjectId());       
    }

    public List<User> getProjectMembers(String projectId, String authHeader) {
        Project project = projectAuthorizationService.authenticateProject(projectId, authHeader);
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        User owner = userService.getUserById(project.getOwnerId());
        List<User> members = new ArrayList<>();
        members.add(owner);
        for (String memberId : project.getProjectMembers()) {
            User member = userRepository.findById(memberId).orElse(null);
            if (member != null) {
                members.add(member);
            }
        }
        return members;
    }


    public String getOwnerIdByProjectId(String projectId) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
            return project.getOwnerId();
        }
    


    public void deleteProjectChanges(String projectId, String authHeader) {
        Project project = projectAuthorizationService.authenticateProject(projectId, authHeader);
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        changeService.deleteChangesByProjectId(projectId);
    }

    public void deleteProjectIdeas(String projectId, String authHeader) {
        Project project = projectAuthorizationService.authenticateProject(projectId, authHeader);
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        ideaRepository.deleteByProjectId(projectId);
    }

    public void makeUserLeaveFromProject(String projectId, String authHeader) {
        Project project = projectAuthorizationService.authenticateProject(projectId, authHeader);
        String userId = userService.getUserIdByToken(authHeader);
        project.getProjectMembers().remove(userId);
        userService.deleteProjectFromUser(userId, projectId);
        projectRepository.save(project);
    }

    public Message sendChatMessage(String projectId, String authHeader, MessageRegister message) {
        projectAuthorizationService.authenticateProject(projectId, authHeader);
        User user = userService.getUserByToken(authHeader);

        Message newMessage = new Message();
        newMessage.setProjectId(projectId);
        newMessage.setSenderId(user.getId());
        newMessage.setUsername(user.getUsername());
        newMessage.setContent(message.getContent());
        newMessage.setCreatedAt(java.time.LocalDateTime.now());
        Message savedMessage = messageRepository.save(newMessage);
        return savedMessage;
    }

    public List<Message> getMessages(String projectId, String authHeader) {
        projectAuthorizationService.authenticateProject(projectId, authHeader);
        List<Message> messages = messageRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
        return messages;
    }
        
}
