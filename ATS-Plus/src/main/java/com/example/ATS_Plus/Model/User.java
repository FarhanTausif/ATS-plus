package com.example.ATS_Plus.Model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;
    @Column(name = "userName", nullable = false)
    private String userName;
    @Column(name ="fullName", nullable = false)
    private String fullName;
    @Column(name="email", nullable = false, unique = true)
    private String email;
    @Column(name="password", nullable = false)
    private String password;
    @Column(name="role", nullable = false)
    private String role;
    @Column(name="activated", nullable = false)
    private boolean activated = false;
    @Column(name = "activation_token")
    private String activationToken;
    @Column(name = "reset_token")
    private String resetToken;
    // One-to-Many relationship with JobRequirement (for HR users)
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<JobRequirement> jobRequirements;
    public enum Roles{
        HR
    }

}
