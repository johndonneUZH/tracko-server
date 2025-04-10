package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.models.project.ProjectRegister;
import ch.uzh.ifi.hase.soprafs24.models.project.ProjectUpdate;
import ch.uzh.ifi.hase.soprafs24.models.user.User;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Optional;
import ch.uzh.ifi.hase.soprafs24.repository.ProjectRepository;
import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import org.springframework.http.HttpStatus;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, JwtUtil jwtUtil, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    public Project createProject(ProjectRegister inputProject, String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);
        List<Project> projects = projectRepository.findByOwnerId(userId);

        for (Project project : projects) {
            if (project.getProjectName().equals(inputProject.getProjectName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Project with this name already exists");
            }
        }

        

        Project newProject = new Project();

        newProject.setOwnerId(userId);
        newProject.setProjectName(inputProject.getProjectName());
        newProject.setProjectDescription(inputProject.getProjectDescription());
        newProject.setProjectMembers(new ArrayList<>());
        newProject.setCreatedAt(java.time.LocalDateTime.now());
        newProject.setUpdatedAt(java.time.LocalDateTime.now());
        newProject.setProjectLogoUrl("University");
        
        Project savedProject = projectRepository.save(newProject);
        userService.addProjectIdToUser(userId, savedProject.getProjectId());

        return savedProject;
    }

    public Project authenticateProject(String projectId, String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);
        Optional<Project> project = projectRepository.findById(projectId);

        if (project.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }

        if (!project.get().getOwnerId().equals(userId) && !project.get().getProjectMembers().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project");
        }

        return project.get();
    }

    public List<Project> getProjectsByUserId(String userId) {
        List<Project> projectOwned = projectRepository.findByOwnerId(userId);
        List<Project> projectMember = projectRepository.findByProjectMembers(userId);
        Set<Project> allProjects = new HashSet<>(projectOwned);
        allProjects.addAll(projectMember);
        return new ArrayList<>(allProjects);

    }

    public Project updateProject(String projectId, ProjectUpdate updatedProject, String authHeader) {
        Project project = authenticateProject(projectId, authHeader);

        String userId = userService.getUserIdByToken(authHeader);
        if (!project.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this project");
        }

        project.setProjectName(updatedProject.getProjectName());
        project.setProjectDescription(updatedProject.getProjectDescription());
        project.setUpdatedAt(java.time.LocalDateTime.now());
        project.setProjectLogoUrl(updatedProject.getProjectLogoUrl());

        // Members logic
        HashSet<String> members = new HashSet<>(project.getProjectMembers());

        if (updatedProject.getMembersToAdd() != null) {
            members.addAll(updatedProject.getMembersToAdd());
        }
        
        if (updatedProject.getMembersToRemove() != null) {
            members.removeAll(updatedProject.getMembersToRemove());
        }
        
        project.setProjectMembers(new ArrayList<>(members));
        
        projectRepository.save(project);
        return project;

    }

    public void deleteProject(String projectId, String authHeader) {
        Project project = authenticateProject(projectId, authHeader);

        String userId = userService.getUserIdByToken(authHeader);
        if (!project.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this project");
        }

        // Remove the project from all members
        for (String memberId : project.getProjectMembers()) {
            User projectmember = userService.getUserById(memberId);
            userService.deleteProjectFromUser(projectmember.getId(), projectId);
        }
        userService.deleteProjectFromUser(userId, projectId);
        projectRepository.deleteById(project.getProjectId());       
    }

}
