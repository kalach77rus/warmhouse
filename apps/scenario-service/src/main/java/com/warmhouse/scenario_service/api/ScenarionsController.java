package com.warmhouse.scenario_service.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scenarios")
public class ScenarionsController {

    @GetMapping("/test")
    public String test() {
        return "Hello from Scenarion service";
    }
    
}
