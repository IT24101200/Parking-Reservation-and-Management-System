package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Show login page
    @GetMapping("/login")
    public String login() {
        return "login"; // maps to login.html (Thymeleaf)
    }

    // Show registration page
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // maps to register.html
    }

    // Handle registration form submission
    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("errorMessage", "Email is already registered!");
            return "register";
        }

        // Validate required fields
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            model.addAttribute("errorMessage", "First name is required!");
            return "register";
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Last name is required!");
            return "register";
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Email is required!");
            return "register";
        }

        // Basic email validation
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            model.addAttribute("errorMessage", "Please enter a valid email address!");
            return "register";
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
            model.addAttribute("errorMessage", "Password is required!");
            return "register";
        }

        if (user.getPasswordHash().length() < 6) {
            model.addAttribute("errorMessage", "Password must be at least 6 characters long!");
            return "register";
        }

        try {
            // Trim and normalize input data
            user.setFirstName(user.getFirstName().trim());
            user.setLastName(user.getLastName().trim());
            user.setEmail(user.getEmail().trim().toLowerCase());

            if (user.getPhoneNumber() != null) {
                user.setPhoneNumber(user.getPhoneNumber().trim());
                if (user.getPhoneNumber().isEmpty()) {
                    user.setPhoneNumber(null);
                }
            }

            // Encrypt password before saving
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

            // Default role is CUSTOMER for self-registration
            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("CUSTOMER");
            } else {
                user.setRole(user.getRole().toUpperCase().trim());
            }

            // Set default status
            if (user.getStatus() == null || user.getStatus().isBlank()) {
                user.setStatus("ACTIVE");
            }

            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            return "redirect:/login?success"; // redirect to login page

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Registration failed. Please try again.");
            return "register";
        }
    }

    // ========================
    // REST API ENDPOINTS FOR TESTING
    // ========================

    // REST API: User Registration
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Check if email already exists
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                response.put("success", false);
                response.put("message", "Email is already registered!");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate required fields
            if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "First name is required!");
                return ResponseEntity.badRequest().body(response);
            }

            if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Last name is required!");
                return ResponseEntity.badRequest().body(response);
            }

            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required!");
                return ResponseEntity.badRequest().body(response);
            }

            // Basic email validation
            if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                response.put("success", false);
                response.put("message", "Please enter a valid email address!");
                return ResponseEntity.badRequest().body(response);
            }

            if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Password is required!");
                return ResponseEntity.badRequest().body(response);
            }

            if (user.getPasswordHash().length() < 6) {
                response.put("success", false);
                response.put("message", "Password must be at least 6 characters long!");
                return ResponseEntity.badRequest().body(response);
            }

            // Trim and normalize input data
            user.setFirstName(user.getFirstName().trim());
            user.setLastName(user.getLastName().trim());
            user.setEmail(user.getEmail().trim().toLowerCase());

            if (user.getPhoneNumber() != null) {
                user.setPhoneNumber(user.getPhoneNumber().trim());
                if (user.getPhoneNumber().isEmpty()) {
                    user.setPhoneNumber(null);
                }
            }

            // Encrypt password before saving
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

            // Default role is CUSTOMER for self-registration
            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("CUSTOMER");
            } else {
                user.setRole(user.getRole().toUpperCase().trim());
            }

            // Set default status
            if (user.getStatus() == null || user.getStatus().isBlank()) {
                user.setStatus("ACTIVE");
            }

            User savedUser = userRepository.save(user);

            response.put("success", true);
            response.put("message", "Registration successful!");
            response.put("userId", savedUser.getUserID());
            response.put("email", savedUser.getEmail());
            response.put("role", savedUser.getRole());
            response.put("status", savedUser.getStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // REST API: User Login Verification
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required!");
                return ResponseEntity.badRequest().body(response);
            }

            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Password is required!");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<User> userOptional = userRepository.findByEmail(email.trim().toLowerCase());

            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid email or password!");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOptional.get();

            // Check if account is active
            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                response.put("success", false);
                response.put("message", "Your account has been temporarily deactivated!");
                return ResponseEntity.badRequest().body(response);
            }

            // Verify password
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                response.put("success", false);
                response.put("message", "Invalid email or password!");
                return ResponseEntity.badRequest().body(response);
            }

            // Login successful
            response.put("success", true);
            response.put("message", "Login successful!");
            response.put("userId", user.getUserID());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("role", user.getRole());
            response.put("status", user.getStatus());

            // Suggest redirect URL based on role
            String redirectUrl = switch (user.getRole().toUpperCase()) {
                case "ADMIN" -> "/admin/dashboard";
                case "CUSTOMER" -> "/customer/dashboard";
                case "PARKING_SLOT_MANAGER" -> "/slotmanager/dashboard";
                case "FINANCE_EXECUTIVE" -> "/finance/dashboard";
                case "SECURITY_OFFICER" -> "/security/dashboard";
                case "CUSTOMER_SUPPORT_OFFICER" -> "/support/dashboard";
                default -> "/";
            };
            response.put("redirectUrl", redirectUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // REST API: Get User Profile
    @GetMapping("/api/user/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<User> userOptional = userRepository.findById(id);

            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found!");
                return ResponseEntity.notFound().build();
            }

            User user = userOptional.get();

            response.put("success", true);
            response.put("userId", user.getUserID());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("phoneNumber", user.getPhoneNumber());
            response.put("role", user.getRole());
            response.put("status", user.getStatus());
            response.put("createdAt", user.getCreated_at());
            response.put("updatedAt", user.getUpdated_at());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve user: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
