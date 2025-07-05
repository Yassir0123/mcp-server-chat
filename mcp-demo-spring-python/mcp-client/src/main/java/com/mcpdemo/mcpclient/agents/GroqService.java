package com.mcpdemo.mcpclient.agents;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Service
public class GroqService {

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<McpSyncClient> mcpClients;

    public GroqService(List<McpSyncClient> mcpClients) {
        this.mcpClients = mcpClients;
    }

    public String getChatResponse(String userMessage) {
        try {
            // Get available MCP tools
            List<Map<String, Object>> tools = getMcpToolsForGroq();

            // Create system prompt that restricts responses to project data only
            String systemPrompt = createSystemPrompt();

            // Create messages with system prompt
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userMessage));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("messages", messages);
            request.put("temperature", 0.1); // Lower temperature for more focused responses
            request.put("max_tokens", 1024);

            // Add tools if available
            if (!tools.isEmpty()) {
                request.put("tools", tools);
                request.put("tool_choice", "auto");
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return handleGroqResponse(response.getBody(), messages);
        } catch (Exception e) {
            return "Error calling Groq API: " + e.getMessage();
        }
    }

    private String createSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI assistant that can ONLY respond using information from the available tools and project data. ");
        prompt.append("IMPORTANT RULES:\n");
        prompt.append("1. You MUST use the provided tools to get information\n");
        prompt.append("2. You CANNOT provide information from your general knowledge\n");
        prompt.append("3. If you cannot find the answer using the available tools, say 'I cannot find that information in the project data'\n");
        prompt.append("4. Always use tools to search for and retrieve relevant information before responding\n");
        prompt.append("5. Base your responses strictly on the data returned by the tools\n\n");

        // Add information about available tools
        prompt.append("Available tools in this project:\n");
        mcpClients.forEach(client -> {
            client.listTools().tools().forEach(tool -> {
                prompt.append("- ").append(tool.name()).append(": ").append(tool.description()).append("\n");
            });
        });

        return prompt.toString();
    }

    private List<Map<String, Object>> getMcpToolsForGroq() {
        List<Map<String, Object>> tools = new ArrayList<>();

        mcpClients.forEach(client -> {
            client.listTools().tools().forEach(tool -> {
                Map<String, Object> groqTool = new HashMap<>();
                groqTool.put("type", "function");

                Map<String, Object> function = new HashMap<>();
                function.put("name", tool.name());
                function.put("description", tool.description());
                function.put("parameters", convertMcpSchemaToGroqSchema(tool.inputSchema()));

                groqTool.put("function", function);
                tools.add(groqTool);
            });
        });

        return tools;
    }

    private Map<String, Object> convertMcpSchemaToGroqSchema(Object inputSchema) {
        // Convert MCP schema to Groq function schema format
        if (inputSchema instanceof Map) {
            return (Map<String, Object>) inputSchema;
        }

        // Default schema if conversion fails
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", new HashMap<>());
        return schema;
    }

    private String handleGroqResponse(Map<String, Object> response, List<Map<String, Object>> messages) {
        if (response == null || !response.containsKey("choices")) {
            return "No response from Groq";
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices.isEmpty()) return "Empty response";

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        // Check if the model wants to call a tool
        if (message.containsKey("tool_calls")) {
            return handleToolCalls(message, messages);
        }

        return (String) message.get("content");
    }

    private String handleToolCalls(Map<String, Object> message, List<Map<String, Object>> messages) {
        try {
            List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) message.get("tool_calls");

            // Add the assistant's message with tool calls to conversation
            messages.add(message);

            // Execute each tool call
            for (Map<String, Object> toolCall : toolCalls) {
                String toolCallId = (String) toolCall.get("id");
                Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
                String functionName = (String) function.get("name");
                String arguments = (String) function.get("arguments");

                // Execute MCP tool
                String toolResult = executeMcpTool(functionName, arguments);

                // Add tool result to messages
                Map<String, Object> toolMessage = new HashMap<>();
                toolMessage.put("role", "tool");
                toolMessage.put("tool_call_id", toolCallId);
                toolMessage.put("content", toolResult);
                messages.add(toolMessage);
            }

            // Make another API call with tool results
            return makeFollowUpCall(messages);

        } catch (Exception e) {
            return "Error handling tool calls: " + e.getMessage();
        }
    }

    private String executeMcpTool(String toolName, String arguments) {
        try {
            // Find the appropriate MCP client and execute the tool
            for (McpSyncClient client : mcpClients) {
                var tools = client.listTools().tools();
                boolean hasTool = tools.stream().anyMatch(tool -> tool.name().equals(toolName));

                if (hasTool) {
                    McpSchema.CallToolResult result = client.callTool(
                            new McpSchema.CallToolRequest(toolName, arguments)
                    );

                    // Extract content from result
                    if (!result.content().isEmpty()) {
                        var content = result.content().get(0);
                        if (content instanceof McpSchema.TextContent) {
                            return ((McpSchema.TextContent) content).text();
                        }
                    }
                    return "Tool executed successfully but returned no content";
                }
            }
            return "Tool not found: " + toolName;
        } catch (Exception e) {
            return "Error executing tool " + toolName + ": " + e.getMessage();
        }
    }

    private String makeFollowUpCall(List<Map<String, Object>> messages) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> request = new HashMap<>();
            request.put("model", model);
            request.put("messages", messages);
            request.put("temperature", 0.1);
            request.put("max_tokens", 1024);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return extractResponseContent(response.getBody());
        } catch (Exception e) {
            return "Error in follow-up call: " + e.getMessage();
        }
    }

    private String extractResponseContent(Map<String, Object> response) {
        if (response == null || !response.containsKey("choices")) {
            return "No response from Groq";
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices.isEmpty()) return "Empty response";
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}