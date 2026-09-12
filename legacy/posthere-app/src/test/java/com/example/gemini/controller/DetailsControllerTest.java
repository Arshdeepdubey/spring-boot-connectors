package com.example.gemini.controller;

import com.example.gemini.dto.DetailsDTO;
import com.example.gemini.service.DetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DetailsController.class)
public class DetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DetailsService detailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private DetailsDTO detailsDTO;

    @BeforeEach
    void setUp() {
        detailsDTO = new DetailsDTO("John", "Doe", "New York");
    }

    @Test
    void testGetAllDetails() throws Exception {
        List<DetailsDTO> detailsList = Arrays.asList(detailsDTO);
        when(detailsService.getAllDetails()).thenReturn(detailsList);

        mockMvc.perform(get("/api/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(detailsList.size()));
    }

    @Test
    void testGetDetailsById() throws Exception {
        when(detailsService.getDetailsById(anyLong())).thenReturn(detailsDTO);

        mockMvc.perform(get("/api/details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fname").value(detailsDTO.getFname()));
    }

    @Test
    void testCreateDetails() throws Exception {
        when(detailsService.createDetails(any(DetailsDTO.class))).thenReturn(detailsDTO);

        mockMvc.perform(post("/api/details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(detailsDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fname").value(detailsDTO.getFname()));
    }
}
