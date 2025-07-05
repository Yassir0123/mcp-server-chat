package com.mcpdemo.mcpclient.agents;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AIAgent {
    private final GroqService groqService;
    private final List<McpSyncClient> mcpClients;

    public AIAgent(GroqService groqService, List<McpSyncClient> mcpClients) {
        this.groqService = groqService;
        this.mcpClients = mcpClients;
    }

    public String prompt(String question) {
        // Log available tools for debugging
        System.out.println("Available MCP tools:");
        mcpClients.forEach(client -> {
            client.listTools().tools().forEach(tool -> {
                System.out.println("- " + tool.name() + ": " + tool.description());
            });
        });

        return groqService.getChatResponse(question);
    }

    public String getAvailableTools() {
        StringBuilder tools = new StringBuilder("Available tools:\n");
        mcpClients.forEach(client -> {
            client.listTools().tools().forEach(tool -> {
                tools.append("- ").append(tool.name()).append(": ").append(tool.description()).append("\n");
            });
        });
        return tools.toString();
    }
}