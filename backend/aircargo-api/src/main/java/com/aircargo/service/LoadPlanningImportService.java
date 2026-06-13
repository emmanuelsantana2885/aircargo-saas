package com.aircargo.service;

import com.aircargo.dto.LoadPlanningImportResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface LoadPlanningImportService {
    LoadPlanningImportResultDTO importLoadPlanning(MultipartFile file) throws IOException;
}
