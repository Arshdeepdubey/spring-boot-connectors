package com.example.gemini.service.impl;

import com.example.gemini.dto.DetailsDTO;
import com.example.gemini.entity.Details;
import com.example.gemini.repository.DetailsRepository;
import com.example.gemini.service.DetailsService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetailsServiceImpl implements DetailsService {

    private final DetailsRepository detailsRepository;

    public DetailsServiceImpl(DetailsRepository detailsRepository) {
        this.detailsRepository = detailsRepository;
    }

    @Override
    public List<DetailsDTO> getAllDetails() {
        return detailsRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DetailsDTO getDetailsById(Long id) {
        Details details = detailsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Details not found with id: " + id));
        return convertToDTO(details);
    }

    @Override
    public DetailsDTO createDetails(DetailsDTO detailsDTO) {
        Details details = convertToEntity(detailsDTO);
        Details savedDetails = detailsRepository.save(details);
        return convertToDTO(savedDetails);
    }

    private DetailsDTO convertToDTO(Details details) {
        return new DetailsDTO(details.getFname(), details.getLname(), details.getCity());
    }

    private Details convertToEntity(DetailsDTO detailsDTO) {
        Details details = new Details();
        details.setFname(detailsDTO.getFname());
        details.setLname(detailsDTO.getLname());
        details.setCity(detailsDTO.getCity());
        return details;
    }
}
