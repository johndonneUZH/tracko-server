package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.hase.soprafs24.models.project.ProjectRegister;
import ch.uzh.ifi.hase.soprafs24.models.project.Project;
import java.util.ArrayList;
import ch.uzh.ifi.hase.soprafs24.repository.ProjectRepository;
import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, JwtUtil jwtUtil, UserService userService) {
        this.projectRepository = projectRepository;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    public Project createProject(ProjectRegister inputProject, String authHeader) {
        String userId = userService.getUserIdByToken(authHeader);

        Project newProject = new Project();

        newProject.setOwnerId(userId);
        newProject.setProjectName(inputProject.getProjectName());
        newProject.setProjectDescription(inputProject.getProjectDescription());
        newProject.setProjectMembers(new ArrayList<>());

        return projectRepository.save(newProject);
    }

}
