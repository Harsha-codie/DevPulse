package ai.cropplanner.recommendation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;
    
    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;
    
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_RETRY_DELAY = 1000; // 1 second
    
    public GeminiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Generate crop recommendations using Gemini API with schema-constrained prompt
     */
    public String generateRecommendations(String prompt) {
        String url = baseUrl + "/models/" + model + ":generateContent?key=" + apiKey;
        
        // Build schema-constrained request
        Map<String, Object> request = buildSchemaConstrainedRequest(prompt);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        return executeWithRetry(url, entity);
    }
    
    /**
     * Build request with schema constraints for consistent JSON output
     */
    private Map<String, Object> buildSchemaConstrainedRequest(String prompt) {
        Map<String, Object> request = new HashMap<>();
        
        // Enhanced prompt with strict JSON schema requirements
        String schemaConstrainedPrompt = buildPromptWithSchema(prompt);
        
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", schemaConstrainedPrompt);
        content.put("parts", List.of(part));
        
        request.put("contents", List.of(content));
        
        // Add generation config for consistent output
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.2); // Low temperature for consistency
        generationConfig.put("topK", 1);
        generationConfig.put("topP", 0.8);
        generationConfig.put("maxOutputTokens", 2048); // Increased from 1024
        request.put("generationConfig", generationConfig);
        
        return request;
    }
    
    /**
     * Build prompt with strict JSON schema requirements
     */
    private String buildPromptWithSchema(String contextPrompt) {
        return contextPrompt + 
            "\n\nIMPORTANT: You must respond with ONLY valid JSON in this exact format:" +
            "\n{" +
            "\n  \"recommendations\": [" +
            "\n    {" +
            "\n      \"crop\": \"string (crop name)\", " +
            "\n      \"score\": number (0-100), " +
            "\n      \"reason\": \"string (explanation)\"" +
            "\n    }" +
            "\n  ]" +
            "\n}" +
            "\n\nReturn 1-3 crop recommendations. Do not include any text outside the JSON.";
    }
    
    /**
     * Execute API call with retry logic for 429 and 5xx errors
     */
    private String executeWithRetry(String url, HttpEntity<Map<String, Object>> entity) {
        Exception lastException = null;
        
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                logger.debug("Gemini API call attempt {} to: {}", attempt + 1, url);
                
                ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    logger.debug("Gemini API call successful");
                    return extractContentFromResponse(response.getBody());
                }
                
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    logger.warn("Gemini API rate limit hit (429), attempt {}/{}", attempt + 1, MAX_RETRIES);
                    lastException = e;
                    if (attempt < MAX_RETRIES - 1) {
                        waitBeforeRetry(attempt);
                        continue;
                    }
                } else {
                    logger.error("Gemini API client error: {}", e.getMessage());
                    throw new RuntimeException("Gemini API client error: " + e.getMessage(), e);
                }
            } catch (HttpServerErrorException e) {
                logger.warn("Gemini API server error ({}), attempt {}/{}", 
                    e.getStatusCode(), attempt + 1, MAX_RETRIES);
                lastException = e;
                if (attempt < MAX_RETRIES - 1) {
                    waitBeforeRetry(attempt);
                    continue;
                }
            } catch (Exception e) {
                logger.error("Unexpected error during Gemini API call: {}", e.getMessage());
                throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
            }
        }
        
        throw new RuntimeException("Gemini API call failed after " + MAX_RETRIES + " attempts", lastException);
    }
    
    /**
     * Extract content from Gemini API response
     */
    private String extractContentFromResponse(String responseBody) {
        try {
            logger.info("========== FULL GEMINI RESPONSE ==========");
            logger.info(responseBody);
            logger.info("==========================================");
            
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            
            // Check for error in response first
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                String errorMsg = error.path("message").asText("Unknown error");
                logger.error("Gemini API returned error: {}", errorMsg);
                throw new RuntimeException("Gemini API error: " + errorMsg);
            }
            
            logger.info("Candidates found: {}", candidates.isArray() ? candidates.size() : "not an array");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                
                logger.info("Parts found: {}", parts.isArray() ? parts.size() : "not an array");
                
                // Check finish reason
                String finishReason = firstCandidate.path("finishReason").asText("");
                if ("MAX_TOKENS".equals(finishReason)) {
                    logger.warn("Gemini response truncated due to MAX_TOKENS - increasing token limit");
                    throw new RuntimeException("Gemini response truncated - prompt too long. Try simplifying the request.");
                }
                
                if (parts.isArray() && parts.size() > 0) {
                    JsonNode firstPart = parts.get(0);
                    String text = firstPart.path("text").asText();
                    
                    logger.info("Text length: {}", text.length());
                    
                    if (!text.isEmpty()) {
                        logger.info("Raw Gemini response text: {}", text);
                        
                        // Remove markdown code block markers if present
                        text = text.trim();
                        
                        // Remove ```json at the start (with possible newline)
                        if (text.startsWith("```json")) {
                            text = text.substring(7).trim();
                        } else if (text.startsWith("```")) {
                            text = text.substring(3).trim();
                        }
                        
                        // Remove ``` at the end (with possible newline before it)
                        if (text.endsWith("```")) {
                            text = text.substring(0, text.length() - 3).trim();
                        }
                        
                        logger.info("Cleaned response text: {}", text);
                        return text;
                    }
                }
            }
            
            logger.error("No valid content found in Gemini response: {}", responseBody);
            throw new RuntimeException("No valid content in Gemini response");
            
        } catch (Exception e) {
            logger.error("Failed to parse Gemini response", e);
            logger.error("Response body was: {}", responseBody);
            throw new RuntimeException("Failed to parse Gemini response: " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Wait before retry with exponential backoff
     */
    private void waitBeforeRetry(int attemptNumber) {
        try {
            long delay = INITIAL_RETRY_DELAY * (long) Math.pow(2, attemptNumber);
            logger.debug("Waiting {}ms before retry", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during retry wait", e);
        }
    }
}