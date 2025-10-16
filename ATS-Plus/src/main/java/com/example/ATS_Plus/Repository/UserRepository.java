package com.example.ATS_Plus.Repository;

import com.example.ATS_Plus.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
