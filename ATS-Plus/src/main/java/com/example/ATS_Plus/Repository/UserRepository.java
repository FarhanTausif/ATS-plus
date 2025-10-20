package com.example.ATS_Plus.Repository;

import com.example.ATS_Plus.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmail(String email);
    User findByUserName(String user_name);
    List<User> findByRole(String role);
}
