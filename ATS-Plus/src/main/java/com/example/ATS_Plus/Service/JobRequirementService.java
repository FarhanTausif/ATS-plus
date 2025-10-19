package com.example.ATS_Plus.Service;

import com.example.ATS_Plus.Model.JobRequirement;
import com.example.ATS_Plus.Repository.JobRequirementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobRequirementService {

    @Autowired
    private JobRequirementRepository jobRequirementRepository;

    public JobRequirement saveJobRequirement(JobRequirement jobRequirement) {
        return jobRequirementRepository.save(jobRequirement);
    }

    public JobRequirement getJobRequirementById(Long id) {
        return jobRequirementRepository.findById(id).orElse(null);
    }

    public List<JobRequirement> getAllJobRequirements() {
        return jobRequirementRepository.findAll();
    }

    public void deleteJobRequirement(Long id) {
        jobRequirementRepository.deleteById(id);
    }

    public List<JobRequirement> getJobRequirementsByUserId(Long userId) {
        return jobRequirementRepository.findByUser_UserId(userId);
    }
}
