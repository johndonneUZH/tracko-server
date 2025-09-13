package tracko.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tracko.auth.JwtUtil;
import tracko.repository.IdeaRepository;
import tracko.repository.MessageRepository;
import tracko.repository.ProjectRepository;
import tracko.repository.UserRepository;
import tracko.constant.ChangeType;
import tracko.models.ai.AnthropicResponseDTO;
import tracko.models.ai.ContentDTO;
import tracko.models.comment.Comment;
import tracko.models.idea.Idea;
import tracko.models.messages.Message;
import tracko.models.messages.MessageRegister;
import tracko.models.project.Project;
import tracko.models.project.ProjectRegister;
import tracko.models.project.ProjectUpdate;
import tracko.models.report.ReportRegister;
import tracko.models.user.User;

@Service
@Transactional
public class ProjectService {

    private final IdeaRepository ideaRepository;

    @Autowired
    private UserRepository userRepository;

    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final ChangeService changeService;
    private final ProjectAuthorizationService projectAuthorizationService;
    private final MessageRepository messageRepository;
    private final AnthropicService anthropicService;
    private final CommentService commentService;
    private final ReportService reportService;

    public ProjectService(ProjectRepository projectRepository, JwtUtil jwtUtil, 
                          UserService userService, ChangeService changeService, 
                          ProjectAuthorizationService projectAuthorizationService, IdeaRepository ideaRepository,
                          MessageRepository messageRepository,
                          AnthropicService anthropicService, @Lazy CommentService commentService,
                          ReportService reportService) {
        this.reportService = reportService;
        this.anthropicService = anthropicService;
        this.commentService = commentService;
        this.messageRepository = messageRepository;
        this.projectAuthorizationService = projectAuthorizationService;
        this.changeService = changeService;
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.ideaRepository = ideaRepository;
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

        boolean isMemberAdded = false;
        boolean isMemberRemoved = false;

        if (updatedProject.getMembersToAdd() != null) {
            members.addAll(updatedProject.getMembersToAdd());
            for (String memberId : updatedProject.getMembersToAdd()) {
                userService.addProjectIdToUser(memberId, project.getProjectId());

            }
            isMemberAdded = true;
        }
        
        if (updatedProject.getMembersToRemove() != null) {
            members.removeAll(updatedProject.getMembersToRemove());
            for (String memberId : updatedProject.getMembersToRemove()) {
                userService.deleteProjectFromUser(memberId, project.getProjectId());
            }
            isMemberRemoved = true;

        }
        
        project.setProjectMembers(new ArrayList<>(members));

        if (isMemberAdded) {
            changeService.markChange(projectId, ChangeType.ADDED_MEMBER, authHeader, false, null);
        } else if (isMemberRemoved) {
            changeService.markChange(projectId, ChangeType.REMOVED_MEMBER, authHeader, false, null);
        } else {
            changeService.markChange(projectId, ChangeType.CHANGED_PROJECT_SETTINGS, authHeader, false, null);
        }
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
        

    public ContentDTO generateReport(String projectId, String authHeader) {
        Project project = projectAuthorizationService.authenticateProject(projectId, authHeader);
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
    
        List<Message> messages = messageRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
        List<Idea> ideas = ideaRepository.findByProjectId(projectId);
    
        List<Comment> comments = new ArrayList<>();
        for (Idea idea : ideas) {
            commentService.getCommentsByIdeaId(idea.getIdeaId()).forEach(comments::add);
        }
    
        String ideaSummary = ideas.stream()
            .map(idea -> String.format(
                "Idea: %s\nDescription: %s\nUpvotes: %d, Downvotes: %d, Comments: %d\n",
                idea.getIdeaName(),
                idea.getIdeaDescription(),
                idea.getUpVotes().size(),
                idea.getDownVotes().size(),
                idea.getComments().size()
            ))
            .collect(Collectors.joining("\n\n"));  // Added empty line between ideas
    
        String commentSummary = comments.stream()
            .map(comment -> String.format(
                "Comment: %s\nOn Idea ID: %s\n",
                comment.getCommentText(),
                comment.getIdeaId()
            ))
            .collect(Collectors.joining("\n\n"));  // Also space between comments
    
        String messageSummary = messages.stream()
            .map(message -> String.format(
                "Message: %s\nSent At: %s\n",
                message.getContent(),
                message.getCreatedAt()
            ))
            .collect(Collectors.joining("\n\n"));
    
        String template = String.format(
            """
            You are an assistant tasked with generating a summary report for a brainstorming session.
    
            Project Name: %s
    
            Ideas Summary:
            %s
    
            Comments Summary:
            %s
    
            Messages Summary:
            %s
    
            Instructions:
            1. Provide a brief, coherent overview of the brainstorming session.
            2. Identify the top 3 ideas based on upvotes and engagement (comments).
            3. For each top idea, mention the Pros and Cons.
            4. Based on comments and discussions, give any useful recommendations for the project.
    
            Important: 
            Format your response as **clean HTML** with:
            - Title as <h2>
            - Subsections as <h3>
            - Bullet points for Pros and Cons
            - Separate paragraphs (<p>) for recommendations
            - Keep it neat, readable and professional.
            - Use <strong> for important points.
            - Avoid using double \n, use single \n for line breaks.
            - Use <br> for line breaks in HTML.
            - Use <ul> and <li> for lists.
    
            Be concise, professional, and insightful.
            """,
            project.getProjectName(),
            ideaSummary,
            commentSummary,
            messageSummary
        );
    
        // Here you would typically send the template to an AI model for processing.
        AnthropicResponseDTO response = anthropicService.generateContent(template);
        ContentDTO aiGeneratedSummary = response.getContent().get(0); 
        String cleanHtml = aiGeneratedSummary.getText().replaceAll("\\n+", "");  // removes all \n
    
        ContentDTO dto = new ContentDTO();
        dto.setType("text");
        dto.setText(cleanHtml);

        ReportRegister reportRegister = new ReportRegister();
        reportRegister.setReportContent(cleanHtml);
        String userId = userService.getUserIdByToken(authHeader);
        reportService.createReport(reportRegister, userId, projectId);

        return dto; 
    }
}
