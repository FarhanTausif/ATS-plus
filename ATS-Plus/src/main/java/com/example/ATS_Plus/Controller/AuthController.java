package com.example.ATS_Plus.Controller;

import com.example.ATS_Plus.Model.User;
import com.example.ATS_Plus.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        return "auth/register";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                       @RequestParam String password,
                       @RequestParam(required = false) String rememberMe,
                       HttpSession session,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            // Find user by email
            User user = userService.findByEmail(email);
            if (user == null) {
                model.addAttribute("error", "Invalid email or password");
                return "auth/login";
            }

            // Check if user is activated
            if (!user.isActivated()) {
                model.addAttribute("error", "Account not activated. Please check your email for activation link.");
                return "auth/login";
            }

            // Verify password
            String hashedPassword = hashPassword(password);
            if (!user.getPassword().equals(hashedPassword)) {
                model.addAttribute("error", "Invalid email or password");
                return "auth/login";
            }

            // Set session attributes
            session.setAttribute("loggedInUser", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userRole", user.getRole());

            // Redirect based on role
            if ("HR".equals(user.getRole())) {
                return "redirect:/hr/dashboard";
            } else {
                return "redirect:/candidate/job-search";
            }

        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "auth/login";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String userName,
                          @RequestParam String fullName,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          @RequestParam String role,
                          @RequestParam(required = false) String agreeTerms,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            // Validate input
            if (userName == null || userName.trim().length() < 3) {
                model.addAttribute("error", "Username must be at least 3 characters long");
                return "auth/register";
            }

            if (fullName == null || fullName.trim().isEmpty()) {
                model.addAttribute("error", "Full name is required");
                return "auth/register";
            }

            if (email == null || !isValidEmail(email)) {
                model.addAttribute("error", "Please enter a valid email address");
                return "auth/register";
            }

            if (password == null || password.length() < 8) {
                model.addAttribute("error", "Password must be at least 8 characters long");
                return "auth/register";
            }

            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match");
                return "auth/register";
            }

            if (agreeTerms == null) {
                model.addAttribute("error", "You must agree to the terms and conditions");
                return "auth/register";
            }

            // Check if user already exists
            if (userService.findByEmail(email) != null) {
                model.addAttribute("error", "An account with this email already exists");
                return "auth/register";
            }

            if (userService.findByUserName(userName) != null) {
                model.addAttribute("error", "Username is already taken");
                return "auth/register";
            }

            // Create new user
            User newUser = User.builder()
                    .userName(userName.trim())
                    .fullName(fullName.trim())
                    .email(email.toLowerCase().trim())
                    .password(hashPassword(password))
                    .role(role)
                    .activated(true) // For demo purposes, auto-activate. In production, send email verification
                    .activationToken(generateToken())
                    .build();

            User savedUser = userService.saveUser(newUser);

            redirectAttributes.addFlashAttribute("success",
                "Account created successfully! You can now log in.");
            return "redirect:/auth/login";

        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been logged out successfully");
        return "redirect:/auth/login";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "auth/forgot-password";
    }

    // Utility methods
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
