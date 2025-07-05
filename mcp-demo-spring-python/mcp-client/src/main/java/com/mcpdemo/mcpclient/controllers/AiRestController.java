package com.mcpdemo.mcpclient.controllers;

import com.mcpdemo.mcpclient.agents.AIAgent;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class AiRestController {
    private AIAgent agent;

    public AiRestController(AIAgent agent) {
        this.agent = agent;
    }
    @GetMapping("/chat")
    public String askAgent(@RequestParam(name = "query", required = true) String query) {
        return agent.prompt(query);
    }
}
    