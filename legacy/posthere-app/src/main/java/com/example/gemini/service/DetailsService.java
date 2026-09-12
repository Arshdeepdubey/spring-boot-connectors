package com.example.gemini.service;

import com.example.gemini.dto.DetailsDTO;
import java.util.List;

public interface DetailsService {
    List<DetailsDTO> getAllDetails();

    DetailsDTO getDetailsById(Long id);

    DetailsDTO createDetails(DetailsDTO detailsDTO);
}
