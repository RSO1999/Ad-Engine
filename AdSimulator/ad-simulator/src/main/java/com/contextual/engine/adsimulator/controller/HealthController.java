package com.contextual.engine.adsimulator.controller;

// 2. Import the necessary Spring annotations
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


// 3. Tell Spring that this class is a REST Controller
@RestController
// 4. Set the base path for all endpoints in this class
@RequestMapping("/api")

public class HealthController {


// 5. Define a method that handles GET requests to "/api/health"
    @GetMapping("/health")
    public String checkHealth() {
        // 6. Return a simple message
        //beacause we used @restcontroller return type will be a json object
        return "{\"status\": \"UP\"}";
    }

    
}
