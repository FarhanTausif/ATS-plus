package com.example.ATS_Plus.Repository;

import com.example.ATS_Plus.Model.CvContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvContentRepository extends JpaRepository<CvContent,Long> {
    public CvContent findByCvFileId(Long cvFileId);
}
