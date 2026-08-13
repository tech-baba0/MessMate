package com.messmate.backend.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.messmate.backend.dto.request.GoogleLoginRequest;
import com.messmate.backend.dto.response.JwtResponse;
import com.messmate.backend.dto.response.MessageResponse;
import com.messmate.backend.entity.AccountStatus;
import com.messmate.backend.entity.Role;
import com.messmate.backend.entity.User;
import com.messmate.backend.repository.UserRepository;
import com.messmate.backend.security.jwt.JwtUtils;
import com.messmate.backend.security.services.GoogleAuthService;
import com.messmate.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody GoogleLoginRequest loginRequest) {
        try {
            GoogleIdToken.Payload payload = googleAuthService.verifyToken(loginRequest.getIdToken());

            String googleSubjectId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            Optional<User> userOpt = userRepository.findByGoogleSubjectId(googleSubjectId);

            User user;
            if (userOpt.isPresent()) {
                user = userOpt.get();
                user.setLastLogin(LocalDateTime.now());
                if ("sumonpal2710@gmail.com".equalsIgnoreCase(email) && !user.getRoles().contains(Role.ROLE_ADMIN)) {
                    user.getRoles().add(Role.ROLE_ADMIN);
                }
                user = userRepository.save(user);
            } else {
                Set<Role> roles = new HashSet<>();
                roles.add(Role.ROLE_USER);
                if ("sumonpal2710@gmail.com".equalsIgnoreCase(email)) {
                    roles.add(Role.ROLE_ADMIN);
                }

                user = User.builder()
                        .googleSubjectId(googleSubjectId)
                        .email(email)
                        .name(name)
                        .profilePhoto(pictureUrl)
                        .accountStatus(AccountStatus.PENDING)
                        .roles(roles)
                        .createdDate(LocalDateTime.now())
                        .lastLogin(LocalDateTime.now())
                        .build();
                user = userRepository.save(user);
            }

            UserDetailsImpl userDetails = UserDetailsImpl.build(user);

            // Set Authentication context manually
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate standard JWT token for app usage
            String jwt = jwtUtils.generateJwtToken(authentication);

            List<String> userRoles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getName(),
                    userRoles));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/make-admin")
    public ResponseEntity<?> makeAdmin(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse(false, "User not found"));
        }
        User user = userOpt.get();
        user.getRoles().add(Role.ROLE_ADMIN);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse(true, "User upgraded to ADMIN"));
    }
}
