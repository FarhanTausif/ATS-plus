package com.example.ATS_Plus.Service;

import com.example.ATS_Plus.Model.CvContent;
import com.example.ATS_Plus.Repository.CvContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CvContentService {
    @Autowired
    private CvContentRepository cvContentRepository;

    public CvContent getCvContentById(Long cvFileId) {
        return cvContentRepository.findById(cvFileId).orElse(null);
    }

    public CvContent saveCvContent(CvContent cvContent) {
        return cvContentRepository.save(cvContent);
    }

    public CvContent getCvContentByCvFileId(Long cvFileId) {
        return cvContentRepository.findByCvFileId(cvFileId);
    }
}
