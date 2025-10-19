package com.example.ATS_Plus.Repository;

import com.example.ATS_Plus.Model.CvFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvFileRepository extends JpaRepository<CvFile,Long> {
    CvFile findByFileName(String fileName);
}
