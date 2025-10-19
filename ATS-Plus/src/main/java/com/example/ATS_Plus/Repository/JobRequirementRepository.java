package com.example.ATS_Plus.Repository;

import com.example.ATS_Plus.Model.JobRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRequirementRepository extends JpaRepository<JobRequirement, Long> {
    List<JobRequirement> findByUser_UserId(Long userId);
    List<JobRequirement> findByJobTitleContainingIgnoreCase(String jobTitle);
}
