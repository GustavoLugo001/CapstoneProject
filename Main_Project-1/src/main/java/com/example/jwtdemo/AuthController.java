package com.example.jwtdemo;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping("/api")
public class AuthController {

	private final UserRepository userRepository;

	@Autowired
	private JwtService jwtService;

	
    public AuthController(UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }
//test to assure that the back-end was function and security measures were not acting up.
    @GetMapping("/test")
    public String testEndpoint() {
        return "This is a public test endpoint.";
    }

//This is not being used when trying to use session 
    @GetMapping("/debug-session")
    public ResponseEntity<String> debugSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false); 
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active session found.");
        }

        // Retrieve the authentication object from the session
        SecurityContext securityContext = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        
        if (securityContext == null || securityContext.getAuthentication() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authentication found in session.");
        }

        Authentication authentication = securityContext.getAuthentication();
        String sessionInfo = "Session ID: " + session.getId() +
                " | Principal: " + authentication.getPrincipal() +
                " | Authorities: " + authentication.getAuthorities() +
                " | Details: " + authentication.getDetails();

        return ResponseEntity.ok(sessionInfo);
    }

    @GetMapping("/protected")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getProtectedData() {
        return ResponseEntity.ok(Map.of("message", "You have access!"));
    }

    //login was also being called, but was not saving session due to thsi assumption that maybe loginw as not being retained
	//when checking it was the fact that session was delting itself due to securtiy of Spring tool. 
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        System.out.println("🔹 Login attempt for username: " + username);

        // Validate user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If passwords are hashed, use PasswordEncoder (otherwise, keep as is)
        if (!user.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        // Generate JWT Token
        String token = jwtService.generateToken(user.getUsername(), user.getRole());
        System.out.println("✅ Generated JWT: " + token);

        // Build the response
        Map<String, Object> response = new HashMap<>();
        response.put("token", "Bearer " + token);  
        response.put("role", user.getRole());
        response.put("username", user.getUsername());

        // Only include adminCode if the user is an admin
        if ("ROLE_ADMIN".equals(user.getRole())) {
            response.put("adminCode", user.getAdminCode());
        }

        return ResponseEntity.ok(response);
    }






    //there were issues with session due to spring tool, assumption was that registration was perhapse fault.
//It was in reality due to the csrf.
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> requestBody) {
        String username = requestBody.get("username");
        String password = requestBody.get("password");
        String email = requestBody.get("email");
        String role = requestBody.getOrDefault("role", "ROLE_USER");
        String adminCode = requestBody.get("adminCode");

        if (username == null || password == null || email == null) {
            return ResponseEntity.badRequest().body("❌ Username, password, and email are required.");
        }
        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_USER")) {
            return ResponseEntity.badRequest().body("❌ Invalid role. Must be 'ROLE_ADMIN' or 'ROLE_USER'.");
        }
        // If registering as an admin generate an admin code if not provided
        if (role.equals("ROLE_ADMIN") && (adminCode == null || adminCode.isEmpty())) {
            adminCode = UUID.randomUUID().toString();
        }
        // Check for duplicate username or email
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("❌ Username is already taken.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("❌ Email is already in use.");
        }
	    
        User admin = null;
        if (role.equals("ROLE_USER")) {
            if (adminCode == null || adminCode.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Admin code is required for User registration.");
            }
            admin = userRepository.findByAdminCode(adminCode)
                    .orElseThrow(() -> new RuntimeException("❌ Invalid Admin code."));
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); 
        user.setEmail(email);
        user.setRole(role);
        user.setAdminCode(role.equals("ROLE_ADMIN") ? adminCode : null);
        if (role.equals("ROLE_USER")) {
            user.setAdmin(admin); 
        }

        userRepository.save(user);

        return ResponseEntity.ok(role.equals("ROLE_ADMIN")
                ? "✅ Admin registered successfully. Your code: " + adminCode
                : "✅ User registered successfully.");
    }

//These were attempts in order to deal with the csrf issue which resulted in no success.
// @PostMapping("/create-account")
//     public ResponseEntity<String> createAccount(@RequestBody AccountRequest accountRequest,@RequestParam String adminUsername,@RequestParam String accessCode) {
//         try {
//             User newUser = new User();
//             newUser.setUsername(accountRequest.getUsername());
//             newUser.setPassword(accountRequest.getPassword());
//             newUser.setEmail(accountRequest.getEmail());
//             newUser.setRoles(Set.of(accountRequest.getRole()));
//             userService.addUser(newUser, adminUsername, accessCode);
//             return ResponseEntity.status(HttpStatus.CREATED).body("Account created successfully");
//         } catch (RuntimeException e) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
//         }
//     }
//     @GetMapping("/api/test")
//     public String test() {
//         return "CSRF is disabled!";
//     }




  
}
