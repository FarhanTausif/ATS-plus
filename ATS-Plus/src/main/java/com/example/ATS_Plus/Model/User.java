package com.example.ATS_Plus.Model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "atsusers") // Changed to "atsusers" to match data.sql and avoid PostgreSQL reserved keyword
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "user_name", nullable = false)
    private String userName;
    @Column(name = "full_name", nullable = false)
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
    public enum Roles{
        HR
    }

}
