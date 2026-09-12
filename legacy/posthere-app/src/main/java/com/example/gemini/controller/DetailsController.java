package com.example.gemini.controller;

import com.example.gemini.dto.DetailsDTO;
import com.example.gemini.service.DetailsService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/details")
public class DetailsController {

    private final DetailsService detailsService;

    public DetailsController(DetailsService detailsService) {
        this.detailsService = detailsService;
    }

    @GetMapping
    public ResponseEntity<List<DetailsDTO>> getAllDetails() {
        return ResponseEntity.ok(detailsService.getAllDetails());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetailsDTO> getDetailsById(@PathVariable Long id) {
        return ResponseEntity.ok(detailsService.getDetailsById(id));
    }

    @PostMapping
    public ResponseEntity<DetailsDTO> createDetails(@RequestBody DetailsDTO detailsDTO) {
        return new ResponseEntity<>(detailsService.createDetails(detailsDTO), HttpStatus.CREATED);
    }
}
