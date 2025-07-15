package ai.shreds.domain.entities;

import ai.shreds.domain.value_objects.DomainRuleType;
import ai.shreds.domain.value_objects.DomainRuleSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Domain entity representing a moderation rule for content validation.
 * This entity encapsulates all the business rules related to content moderation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainModerationRuleEntity {
    private String ruleId;
    private String ruleName;
    private DomainRuleType ruleType;
    private String description;
    private String ruleExpression;
    private DomainRuleSeverity severity;
    private Boolean isActive;
    private LocalDateTime createdAt;

    /**
     * Validates the rule data to ensure it meets business requirements.
     * 
     * @throws IllegalArgumentException if the rule data is invalid
     */
    public void validate() {
        if (ruleName == null || ruleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule name cannot be null or empty");
        }
        
        if (ruleType == null) {
            throw new IllegalArgumentException("Rule type cannot be null");
        }
        
        if (ruleExpression == null || ruleExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule expression cannot be null or empty");
        }
        
        if (severity == null) {
            throw new IllegalArgumentException("Rule severity cannot be null");
        }
        
        // Validate regex pattern if the rule type uses regular expressions
        if (ruleType == DomainRuleType.CONTENT_VALIDATION) {
            validateRegexPattern();
        }
    }

    /**
     * Validates that the rule expression is a valid regex pattern.
     * 
     * @throws IllegalArgumentException if the regex pattern is invalid
     */
    private void validateRegexPattern() {
        try {
            Pattern.compile(ruleExpression);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException(
                String.format("Invalid regex pattern in rule expression: %s", e.getMessage())
            );
        }
    }

    /**
     * Executes the moderation rule against the provided content.
     * 
     * @param content the content to validate
     * @return true if the content passes the rule, false if it violates the rule
     * @throws IllegalArgumentException if content is null or empty
     * @throws IllegalStateException if the rule is not active
     */
    public boolean execute(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        
        if (!isActive) {
            throw new IllegalStateException("Cannot execute inactive rule");
        }
        
        return switch (ruleType) {
            case CONTENT_VALIDATION -> executeContentValidation(content);
            case DUPLICATE_CHECK -> executeDuplicateCheck(content);
        };
    }

    /**
     * Executes content validation rules using regex pattern matching.
     * 
     * @param content the content to validate
     * @return true if content passes validation, false otherwise
     */
    private boolean executeContentValidation(String content) {
        try {
            Pattern pattern = Pattern.compile(ruleExpression, Pattern.CASE_INSENSITIVE);
            // For content validation, we typically look for violations
            // If the pattern matches, it means the content violates the rule
            return !pattern.matcher(content).find();
        } catch (PatternSyntaxException e) {
            // If regex is invalid, consider it a rule failure for safety
            return false;
        }
    }

    /**
     * Executes duplicate check rules.
     * Note: This is a simplified implementation. In a real system, this would
     * involve more sophisticated similarity algorithms and database queries.
     * 
     * @param content the content to check for duplicates
     * @return true if content is not a duplicate, false otherwise
     */
    private boolean executeDuplicateCheck(String content) {
        // This is a placeholder implementation
        // In a real system, this would check against a database of existing content
        // and use similarity algorithms to detect duplicates
        
        // For now, we'll use a simple length-based check as an example
        // This would be replaced with actual duplicate detection logic
        return content.length() >= 10; // Minimum content length requirement
    }

    /**
     * Checks if this rule should be executed based on its active status.
     * 
     * @return true if the rule should be executed
     */
    public boolean shouldExecute() {
        return isActive != null && isActive;
    }

    /**
     * Activates the rule for execution.
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivates the rule to prevent execution.
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Checks if this rule is critical and should block approval.
     * 
     * @return true if the rule is critical
     */
    public boolean isCritical() {
        return severity == DomainRuleSeverity.CRITICAL;
    }

    /**
     * Gets the timeout for rule execution based on rule type.
     * 
     * @return timeout in milliseconds
     */
    public long getExecutionTimeout() {
        return ruleType.getRecommendedTimeoutMs();
    }

    /**
     * Initializes the rule with default values.
     */
    public void initializeRule() {
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (isActive == null) {
            this.isActive = true;
        }
    }

    /**
     * Creates a content validation rule.
     * 
     * @param ruleName the name of the rule
     * @param description the description of the rule
     * @param regexPattern the regex pattern to match violations
     * @param severity the severity level
     * @return a new moderation rule entity
     */
    public static DomainModerationRuleEntity createContentValidationRule(
            String ruleName, 
            String description, 
            String regexPattern, 
            DomainRuleSeverity severity) {
        return DomainModerationRuleEntity.builder()
                .ruleName(ruleName)
                .ruleType(DomainRuleType.CONTENT_VALIDATION)
                .description(description)
                .ruleExpression(regexPattern)
                .severity(severity)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a duplicate check rule.
     * 
     * @param ruleName the name of the rule
     * @param description the description of the rule
     * @param checkExpression the expression for duplicate checking
     * @param severity the severity level
     * @return a new moderation rule entity
     */
    public static DomainModerationRuleEntity createDuplicateCheckRule(
            String ruleName, 
            String description, 
            String checkExpression, 
            DomainRuleSeverity severity) {
        return DomainModerationRuleEntity.builder()
                .ruleName(ruleName)
                .ruleType(DomainRuleType.DUPLICATE_CHECK)
                .description(description)
                .ruleExpression(checkExpression)
                .severity(severity)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}