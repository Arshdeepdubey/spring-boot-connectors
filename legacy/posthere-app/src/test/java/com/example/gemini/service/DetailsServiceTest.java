package com.example.gemini.service;

import com.example.gemini.dto.DetailsDTO;
import com.example.gemini.entity.Details;
import com.example.gemini.repository.DetailsRepository;
import com.example.gemini.service.impl.DetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DetailsServiceTest {

    @Mock
    private DetailsRepository detailsRepository;

    @InjectMocks
    private DetailsServiceImpl detailsService;

    private Details details;
    private DetailsDTO detailsDTO;

    @BeforeEach
    void setUp() {
        details = new Details(1L, "John", "Doe", "New York");
        detailsDTO = new DetailsDTO("John", "Doe", "New York");
    }

    @Test
    void testGetAllDetails() {
        when(detailsRepository.findAll()).thenReturn(Arrays.asList(details));

        List<DetailsDTO> result = detailsService.getAllDetails();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFname());
    }

    @Test
    void testGetDetailsById() {
        when(detailsRepository.findById(1L)).thenReturn(Optional.of(details));

        DetailsDTO result = detailsService.getDetailsById(1L);

        assertNotNull(result);
        assertEquals("John", result.getFname());
    }

    @Test
    void testCreateDetails() {
        when(detailsRepository.save(any(Details.class))).thenReturn(details);

        DetailsDTO result = detailsService.createDetails(detailsDTO);

        assertNotNull(result);
        assertEquals("John", result.getFname());
    }
}
