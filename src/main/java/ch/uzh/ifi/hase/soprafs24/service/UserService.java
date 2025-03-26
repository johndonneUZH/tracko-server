package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.auth.JwtUtil;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.models.User;
import ch.uzh.ifi.hase.soprafs24.models.UserLogin;
import ch.uzh.ifi.hase.soprafs24.models.UserRegister;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ch.uzh.ifi.hase.soprafs24.constant.LoginStatus;

import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);
  private final JwtUtil jwtUtil;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  UserService(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Autowired
  private UserRepository userRepository;

  public List<User> getUsers() {
    return userRepository.findAll();
  } 

  public boolean checkIfUserExists(UserRegister newUser) {
    User userByUsername = userRepository.findByUsername(newUser.getUsername());
    User userByEmail = userRepository.findByEmail(newUser.getEmail());

    if (userByUsername != null || userByEmail!= null) {
      return true;
    }
    return false;
  }

  public User createUser(UserRegister newUser) {
    User toBeSavedUser = new User();
    toBeSavedUser.setUsername(newUser.getUsername());
    toBeSavedUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
    toBeSavedUser.setEmail(newUser.getEmail());
    toBeSavedUser.setName(newUser.getName());
    toBeSavedUser.setStatus(UserStatus.ONLINE);

    toBeSavedUser = userRepository.save(toBeSavedUser);

    log.debug("Created Information for User: " + newUser);
    return toBeSavedUser;
  }

    public LoginStatus checkLoginRequest(UserLogin loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername());
        if (user == null) {
            return LoginStatus.USER_NOT_FOUND;
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return LoginStatus.INVALID_PASSWORD;
        }
        return LoginStatus.SUCCESS;
    }


    public HashMap<String, String> getTokenById(UserLogin loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername());
        
        if (user == null) { 
          return null; 
        }

        String userId = user.getId().toString();

        HashMap<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", jwtUtil.generateToken(userId));
        tokenMap.put("userId", userId);

        return tokenMap;
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }
 
}