package controller;
import models.project.Project;
import models.report.Report;
import models.report.ReportRegister;
import models.user.User;
import models.user.UserUpdate;
import service.ReportService;
import service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  private final ReportService reportService;
  
  UserController(UserService userService, ReportService reportService) {
    this.userService = userService;
    this.reportService = reportService;
  }

  @GetMapping("")  
  public List<User> getUsers() {
    return userService.getUsers();
  }
  
  @GetMapping("/{userId}")  
  public ResponseEntity<User> getUser(@PathVariable String userId) {
    User user = userService.getUserById(userId);
    if (user == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    return ResponseEntity.status(HttpStatus.OK).body(user);
  }

  @PutMapping("/{userId}")  
  public ResponseEntity<User> updateUser(
          @PathVariable String userId,
          @RequestBody UserUpdate updatedUser,
          @RequestHeader("Authorization") String authHeader) {
  
      userService.authenticateUser(userId, authHeader);
  
      User user = userService.updateUser(userId, updatedUser);
      if (user == null) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
  
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(user);
  }
  
  @GetMapping("/{userId}/projects")  
  public ResponseEntity<List<Project>> getUserProjects(@PathVariable String userId) {
    List<Project> projects = userService.getUserProjects(userId);
    if (projects == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
    }
    return ResponseEntity.status(HttpStatus.OK).body(projects);
  }

  @GetMapping("/{userId}/friends")  
  public ResponseEntity<List<User>> getUserFriends(@PathVariable String userId, @RequestHeader("Authorization") String authHeader) {
    List<User> friends = userService.getUserFriends(userId);
    return ResponseEntity.status(HttpStatus.OK).body(friends);
  }

  @PostMapping("/{userId}/friends/invite/{friendId}")  
  public ResponseEntity<Void> inviteFriend(@PathVariable String userId, @PathVariable String friendId, @RequestHeader("Authorization") String authHeader) {
    userService.inviteFriend(userId, friendId, authHeader);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{userId}/friends/accept/{friendId}")
  public ResponseEntity<Void> acceptFriend(@PathVariable String userId, @PathVariable String friendId, @RequestHeader("Authorization") String authHeader) {
    userService.acceptFriend(userId, friendId, authHeader);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{userId}/friends/reject/{friendId}")
  public ResponseEntity<Void> rejectFriend(@PathVariable String userId, @PathVariable String friendId, @RequestHeader("Authorization") String authHeader) {
    userService.rejectFriend(userId, friendId, authHeader);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{userId}/friends/remove/{friendId}")
  public ResponseEntity<Void> removeFriend(@PathVariable String userId, @PathVariable String friendId, @RequestHeader("Authorization") String authHeader) {
    userService.removeFriend(userId, friendId, authHeader);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/{userId}/friends/cancel/{friendId}")
  public ResponseEntity<Void> cancelFriendRequest(@PathVariable String userId, @PathVariable String friendId, @RequestHeader("Authorization") String authHeader) {
    userService.cancelFriendRequest(userId, friendId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/{userId}/reports")
  public ResponseEntity<List<Report>> getUserReports(@PathVariable String userId, @RequestHeader("Authorization") String authHeader) {
    userService.authenticateUser(userId, authHeader);
    List<Report> reports = reportService.getReportsByUserId(userId);
    if (reports == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
    }
    return ResponseEntity.status(HttpStatus.OK).body(reports);
  }
  
  @PutMapping("/{userId}/reports/{reportId}") 
    public ResponseEntity<Void> updateReport(
            @PathVariable String userId,
            @PathVariable String reportId,
            @RequestBody ReportRegister updatedReport,
            @RequestHeader("Authorization") String authHeader) {
  
        userService.authenticateUser(userId, authHeader);
        reportService.updateReport(updatedReport, reportId);
  
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @GetMapping("/{userId}/reports/{reportId}")
  public ResponseEntity<Report> getReport(@PathVariable String userId, @PathVariable String reportId, @RequestHeader("Authorization") String authHeader) {
    userService.authenticateUser(userId, authHeader);
    Report report = reportService.getReportById(reportId);
    if (report == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    return ResponseEntity.status(HttpStatus.OK).body(report);
  }

  
}


