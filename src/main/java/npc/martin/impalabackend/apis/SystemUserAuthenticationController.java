package npc.martin.impalabackend.apis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import npc.martin.impalabackend.advice.ResourceNotFoundException;
import npc.martin.impalabackend.entity.Role;
import npc.martin.impalabackend.entity.SystemRoles;
import npc.martin.impalabackend.entity.SystemUser;
import npc.martin.impalabackend.payload.request.LoginRequest;
import npc.martin.impalabackend.payload.request.ResetPasswordRequest;
import npc.martin.impalabackend.payload.request.SignupRequest;
import npc.martin.impalabackend.payload.response.JwtResponse;
import npc.martin.impalabackend.payload.response.MessageResponse;
import npc.martin.impalabackend.repository.SystemUserRepository;
import npc.martin.impalabackend.security.jwt.JwtUtils;
import npc.martin.impalabackend.services.SystemUserDetailsImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import npc.martin.impalabackend.repository.RoleRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;

/**
 *
 * @author bikathi_martin
 */

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173" })
public class SystemUserAuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private SystemUserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    private final Logger logger = LoggerFactory.getLogger(SystemUserAuthenticationController.class);
    
    @PostMapping(value = "/auth/signin")
    public ResponseEntity<?> signInUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getStaffId(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = "Bearer " + jwtUtils.generateJwtToken(authentication);
        
        SystemUserDetailsImpl userDetails = (SystemUserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());
        logger.info("Successfully logged in user: {}", userDetails.getStaffId());
        
        return new ResponseEntity<>(new JwtResponse(
            jwt, 
            userDetails.getStaffId(), 
            userDetails.getFirstName(), 
            roles), HttpStatus.OK);
    }
    
    @PostMapping(value = "/auth/signup")
    public ResponseEntity<?> signUpUser(@RequestBody SignupRequest signupRequest) {
        if(userRepository.existsByStaffId(signupRequest.getStaffId())) {
            return new ResponseEntity<>(new MessageResponse("Error: StaffId is Already Taken"), 
                HttpStatus.IM_USED);
        }
        
        //create a new user's account
        SystemUser user = new SystemUser(
                signupRequest.getStaffId(), 
                signupRequest.getFirstName(), 
                signupRequest.getLastName(), 
                passwordEncoder.encode(signupRequest.getPassword()), 
                signupRequest.getAccountActive(),
                null);
        
        Set<String> stringRoles = signupRequest.getStaffRoles();
        Set<Role> roles = new HashSet<>();
        
        if(stringRoles == null) {
            Role userRole = roleRepository.findByRoleName(SystemRoles.ROLE_EDITOR)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
            roles.add(userRole);
        } else {
            stringRoles.forEach(role -> {
                switch(role) {
                    case "admin" -> {
                        Role adminRole = roleRepository.findByRoleName(SystemRoles.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is Not Found"));
                        roles.add(adminRole);
                        break;
                    }
                    case "editor" -> {
                        Role editorRole = roleRepository.findByRoleName(SystemRoles.ROLE_EDITOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is Not Found"));
                        roles.add(editorRole);
                        break;
                    }
                    case "manager" -> {
                        Role managerRole = roleRepository.findByRoleName(SystemRoles.ROLE_MANAGER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is Not Found"));
                        roles.add(managerRole);
                        break;
                    }
                    default -> {
                        Role editorRoleDefault = roleRepository.findByRoleName(SystemRoles.ROLE_EDITOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is Not Found"));
                        roles.add(editorRoleDefault);
                    }
                }
            });
        }
        
        user.setStaffRoles(roles);
        userRepository.save(user);
        return new ResponseEntity<>(new MessageResponse("User registered successfully"),HttpStatus.CREATED);
    }
    
    @PutMapping(value = "/auth/passReset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest passwordReset) {
        SystemUser user = userRepository.findByStaffId(passwordReset.getStaffId())
            .orElseThrow(() -> 
                new ResourceNotFoundException("Staff With Id " + passwordReset.getStaffId() + "Not Found"));
        
        user.setPassword(passwordEncoder.encode(passwordReset.getNewPassword()));
        try {
            userRepository.save(user);
            return new ResponseEntity<>(new MessageResponse("Reset Password Successfully"), HttpStatus.OK);
        } catch(Exception ex) {
            return new ResponseEntity<>(new RuntimeException("Error Reseting Password"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
