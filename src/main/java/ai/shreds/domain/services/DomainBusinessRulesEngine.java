package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainApprovalRequestEntity;
import ai.shreds.domain.entities.DomainModerationRuleEntity;
import ai.shreds.domain.exceptions.DomainBusinessRuleViolationException;
import ai.shreds.domain.ports.DomainOutputPortModerationRuleRepository;
import ai.shreds.domain.value_objects.DomainRuleSeverity;
import ai.shreds.domain.value_objects.DomainRuleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Domain service for executing business rules against approval requests.
 * This service implements the configurable validation engine for quote approval criteria.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DomainBusinessRulesEngine {
    
    private final DomainOutputPortModerationRuleRepository moderationRuleRepository;
    
    // Cache for frequently accessed rules
    private final Map<String, List<DomainModerationRuleEntity>> ruleCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY_MINUTES = 30;
    private volatile long lastCacheUpdate = 0;
    
    /**
     * Executes all active moderation rules against an approval request.
     * 
     * @param request the approval request to validate
     * @return a map containing rule execution results with rule names as keys and results as values
     * @throws IllegalArgumentException if the request is null
     * @throws DomainBusinessRuleViolationException if critical rules are violated
     */
    public Map<String, Object> executeRules(DomainApprovalRequestEntity request) {
        validateRequest(request);
        
        List<DomainModerationRuleEntity> activeRules = getActiveRules();
        Map<String, Object> executionResults = new HashMap<>();
        
        log.info("Executing {} rules for approval request {}", activeRules.size(), request.getApprovalRequestId());
        
        // Execute content validation rules in parallel
        List<DomainModerationRuleEntity> contentRules = activeRules.stream()
                .filter(rule -> rule.getRuleType() == DomainRuleType.CONTENT_VALIDATION)
                .collect(Collectors.toList());
        
        // Execute duplicate check rules sequentially (they might require database access)
        List<DomainModerationRuleEntity> duplicateRules = activeRules.stream()
                .filter(rule -> rule.getRuleType() == DomainRuleType.DUPLICATE_CHECK)
                .collect(Collectors.toList());
        
        // Execute content validation rules in parallel
        Map<String, Object> contentResults = executeContentValidationRules(contentRules, request);
        executionResults.putAll(contentResults);
        
        // Execute duplicate check rules sequentially
        Map<String, Object> duplicateResults = executeDuplicateCheckRules(duplicateRules, request);
        executionResults.putAll(duplicateResults);
        
        // Check for critical rule violations
        checkCriticalViolations(executionResults, activeRules);
        
        log.info("Rule execution completed for approval request {} with {} results", 
                request.getApprovalRequestId(), executionResults.size());
        
        return executionResults;
    }
    
    /**
     * Validates content against all active content validation rules.
     * 
     * @param content the content to validate
     * @return true if content passes all validation rules
     * @throws IllegalArgumentException if content is null or empty
     */
    public boolean validateContent(String content) {
        validateContentParameter(content);
        
        List<DomainModerationRuleEntity> contentRules = getActiveRules().stream()
                .filter(rule -> rule.getRuleType() == DomainRuleType.CONTENT_VALIDATION)
                .collect(Collectors.toList());
        
        for (DomainModerationRuleEntity rule : contentRules) {
            try {
                boolean passed = rule.execute(content);
                if (!passed) {
                    log.warn("Content validation failed for rule: {}", rule.getRuleName());
                    if (rule.getSeverity() == DomainRuleSeverity.CRITICAL) {
                        return false;
                    }
                }
            } catch (Exception e) {
                log.error("Error executing content validation rule {}: {}", rule.getRuleName(), e.getMessage());
                // For safety, treat rule execution errors as failures for critical rules
                if (rule.getSeverity() == DomainRuleSeverity.CRITICAL) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Checks for duplicate content using similarity algorithms.
     * 
     * @param content the content to check for duplicates
     * @return true if content is not a duplicate
     * @throws IllegalArgumentException if content is null or empty
     */
    public boolean checkDuplicates(String content) {
        validateContentParameter(content);
        
        List<DomainModerationRuleEntity> duplicateRules = getActiveRules().stream()
                .filter(rule -> rule.getRuleType() == DomainRuleType.DUPLICATE_CHECK)
                .collect(Collectors.toList());
        
        for (DomainModerationRuleEntity rule : duplicateRules) {
            try {
                boolean passed = rule.execute(content);
                if (!passed) {
                    log.warn("Duplicate check failed for rule: {}", rule.getRuleName());
                    return false;
                }
            } catch (Exception e) {
                log.error("Error executing duplicate check rule {}: {}", rule.getRuleName(), e.getMessage());
                // For safety, treat rule execution errors as failures
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calculates a quality score for the content based on various criteria.
     * 
     * @param content the content to score
     * @return a quality score between 0.0 and 1.0
     * @throws IllegalArgumentException if content is null or empty
     */
    public Double calculateQualityScore(String content) {
        validateContentParameter(content);
        
        double score = 0.0;
        int totalCriteria = 0;
        
        // Content length scoring (20% weight)
        totalCriteria++;
        if (content.length() >= 10 && content.length() <= 500) {
            score += 0.2;
        } else if (content.length() > 500) {
            score += 0.1; // Penalty for being too long
        }
        
        // Grammar and readability scoring (30% weight)
        totalCriteria++;
        double grammarScore = calculateGrammarScore(content);
        score += grammarScore * 0.3;
        
        // Originality scoring (30% weight)
        totalCriteria++;
        double originalityScore = calculateOriginalityScore(content);
        score += originalityScore * 0.3;
        
        // Attribution quality scoring (20% weight)
        totalCriteria++;
        double attributionScore = calculateAttributionScore(content);
        score += attributionScore * 0.2;
        
        // Normalize the score to ensure it's between 0.0 and 1.0
        score = Math.max(0.0, Math.min(1.0, score));
        
        log.debug("Quality score calculated for content: {}", score);
        
        return score;
    }
    
    /**
     * Executes content validation rules in parallel.
     * 
     * @param rules the content validation rules to execute
     * @param request the approval request to validate
     * @return a map of rule execution results
     */
    private Map<String, Object> executeContentValidationRules(List<DomainModerationRuleEntity> rules, DomainApprovalRequestEntity request) {
        Map<String, Object> results = new ConcurrentHashMap<>();
        
        // Create a simple content string for validation (in a real system, this would be more sophisticated)
        String content = createContentForValidation(request);
        
        List<CompletableFuture<Void>> futures = rules.stream()
                .map(rule -> CompletableFuture.runAsync(() -> {
                    try {
                        boolean passed = rule.execute(content);
                        results.put(rule.getRuleName(), Map.of(
                                "passed", passed,
                                "ruleType", rule.getRuleType().toString(),
                                "severity", rule.getSeverity().toString(),
                                "message", passed ? "Rule passed" : "Rule failed"
                        ));
                    } catch (Exception e) {
                        log.error("Error executing rule {}: {}", rule.getRuleName(), e.getMessage());
                        results.put(rule.getRuleName(), Map.of(
                                "passed", false,
                                "ruleType", rule.getRuleType().toString(),
                                "severity", rule.getSeverity().toString(),
                                "message", "Rule execution failed: " + e.getMessage()
                        ));
                    }
                }))
                .collect(Collectors.toList());
        
        // Wait for all rules to complete with timeout
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(30, TimeUnit.SECONDS)
                .join();
        
        return results;
    }
    
    /**
     * Executes duplicate check rules sequentially.
     * 
     * @param rules the duplicate check rules to execute
     * @param request the approval request to validate
     * @return a map of rule execution results
     */
    private Map<String, Object> executeDuplicateCheckRules(List<DomainModerationRuleEntity> rules, DomainApprovalRequestEntity request) {
        Map<String, Object> results = new HashMap<>();
        
        // Create a simple content string for validation
        String content = createContentForValidation(request);
        
        for (DomainModerationRuleEntity rule : rules) {
            try {
                boolean passed = rule.execute(content);
                results.put(rule.getRuleName(), Map.of(
                        "passed", passed,
                        "ruleType", rule.getRuleType().toString(),
                        "severity", rule.getSeverity().toString(),
                        "message", passed ? "No duplicates found" : "Possible duplicate detected"
                ));
            } catch (Exception e) {
                log.error("Error executing duplicate check rule {}: {}", rule.getRuleName(), e.getMessage());
                results.put(rule.getRuleName(), Map.of(
                        "passed", false,
                        "ruleType", rule.getRuleType().toString(),
                        "severity", rule.getSeverity().toString(),
                        "message", "Duplicate check failed: " + e.getMessage()
                ));
            }
        }
        
        return results;
    }
    
    /**
     * Checks for critical rule violations and throws an exception if found.
     * 
     * @param results the rule execution results
     * @param rules the executed rules
     * @throws DomainBusinessRuleViolationException if critical rules are violated
     */
    private void checkCriticalViolations(Map<String, Object> results, List<DomainModerationRuleEntity> rules) {
        for (DomainModerationRuleEntity rule : rules) {
            if (rule.getSeverity() == DomainRuleSeverity.CRITICAL) {
                Object result = results.get(rule.getRuleName());
                if (result instanceof Map) {
                    Map<String, Object> ruleResult = (Map<String, Object>) result;
                    Boolean passed = (Boolean) ruleResult.get("passed");
                    if (passed == null || !passed) {
                        String message = (String) ruleResult.get("message");
                        throw new DomainBusinessRuleViolationException(rule.getRuleName(), message);
                    }
                }
            }
        }
    }
    
    /**
     * Gets all active moderation rules with caching.
     * 
     * @return a list of active moderation rules
     */
    private List<DomainModerationRuleEntity> getActiveRules() {
        long currentTime = System.currentTimeMillis();
        
        // Check if cache is still valid
        if (currentTime - lastCacheUpdate < TimeUnit.MINUTES.toMillis(CACHE_EXPIRY_MINUTES)) {
            List<DomainModerationRuleEntity> cachedRules = ruleCache.get("active");
            if (cachedRules != null) {
                return cachedRules;
            }
        }
        
        // Fetch fresh rules from repository
        List<DomainModerationRuleEntity> activeRules = moderationRuleRepository.findActiveRules();
        
        // Update cache
        ruleCache.put("active", activeRules);
        lastCacheUpdate = currentTime;
        
        return activeRules;
    }
    
    /**
     * Creates a content string for validation from the approval request.
     * 
     * @param request the approval request
     * @return a string representation of the content to validate
     */
    private String createContentForValidation(DomainApprovalRequestEntity request) {
        // In a real system, this would fetch the actual quote content from the Quote Catalog
        // For now, we'll use the request ID as a placeholder
        return "Quote content for request: " + request.getApprovalRequestId();
    }
    
    /**
     * Calculates a grammar score for the content.
     * 
     * @param content the content to score
     * @return a grammar score between 0.0 and 1.0
     */
    private double calculateGrammarScore(String content) {
        // Simplified grammar scoring logic
        // In a real system, this would use more sophisticated NLP libraries
        
        double score = 1.0;
        
        // Basic checks
        if (!content.trim().endsWith(".") && !content.trim().endsWith("!") && !content.trim().endsWith("?")) {
            score -= 0.1; // No proper ending punctuation
        }
        
        // Check for basic capitalization
        if (Character.isLowerCase(content.trim().charAt(0))) {
            score -= 0.1; // Doesn't start with capital letter
        }
        
        // Check for excessive punctuation
        long punctuationCount = content.chars().filter(ch -> "!@#$%^&*()_+-=[]{};':,.<>?".indexOf(ch) >= 0).count();
        if (punctuationCount > content.length() * 0.1) {
            score -= 0.2; // Too much punctuation
        }
        
        return Math.max(0.0, score);
    }
    
    /**
     * Calculates an originality score for the content.
     * 
     * @param content the content to score
     * @return an originality score between 0.0 and 1.0
     */
    private double calculateOriginalityScore(String content) {
        // Simplified originality scoring logic
        // In a real system, this would check against a database of existing quotes
        
        // For now, we'll use content length and uniqueness as proxies
        double score = 1.0;
        
        // Penalty for very short content
        if (content.length() < 20) {
            score -= 0.3;
        }
        
        // Penalty for repetitive content
        String[] words = content.toLowerCase().split("\\s+");
        Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
        double uniquenessRatio = (double) uniqueWords.size() / words.length;
        
        if (uniquenessRatio < 0.7) {
            score -= 0.2; // Too much repetition
        }
        
        return Math.max(0.0, score);
    }
    
    /**
     * Calculates an attribution score for the content.
     * 
     * @param content the content to score
     * @return an attribution score between 0.0 and 1.0
     */
    private double calculateAttributionScore(String content) {
        // Simplified attribution scoring logic
        // In a real system, this would verify source attribution
        
        double score = 1.0;
        
        // Check for common attribution patterns
        if (content.contains("—") || content.contains("–") || content.contains("-")) {
            score += 0.0; // Has attribution marker
        } else {
            score -= 0.2; // No clear attribution
        }
        
        return Math.max(0.0, score);
    }
    
    // Validation methods
    private void validateRequest(DomainApprovalRequestEntity request) {
        if (request == null) {
            throw new IllegalArgumentException("Approval request cannot be null");
        }
    }
    
    private void validateContentParameter(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
    }
}