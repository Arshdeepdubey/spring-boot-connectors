package com.example.gemini.config;

import com.example.gemini.entity.Details;
import com.example.gemini.repository.DetailsRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {

    @Bean
    public CommandLineRunner initDatabase(DetailsRepository repository) {
        return args -> {
            // save a few customers
            repository.save(new Details(null, "Arshdeep", "Dubey", "Delhi"));
            repository.save(new Details(null, "Jack", "Bauer", "New York"));
            repository.save(new Details(null, "Chloe", "O'Brian", "Los Angeles"));
            repository.save(new Details(null, "Kim", "Bauer", "Chicago"));
            repository.save(new Details(null, "David", "Palmer", "Washington D.C."));
            repository.save(new Details(null, "Michelle", "Dessler", "Miami"));
        };
    }
}
