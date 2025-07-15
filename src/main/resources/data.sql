-- Initial data for Quote Approval Workflow

-- Insert default approval queues
INSERT INTO approval_queues (queue_id, queue_name, max_capacity, current_size, is_active, created_at)
VALUES 
    (gen_random_uuid(), 'General Queue', 1000, 0, true, NOW()),
    (gen_random_uuid(), 'High Priority Queue', 500, 0, true, NOW()),
    (gen_random_uuid(), 'Urgent Queue', 100, 0, true, NOW())
ON CONFLICT (queue_name) DO NOTHING;

-- Insert default moderation rules
INSERT INTO moderation_rules (rule_id, rule_name, rule_type, description, rule_expression, severity, is_active, created_at)
VALUES 
    (gen_random_uuid(), 'Profanity Check Rule', 'CONTENT_VALIDATION', 'Checks for inappropriate language and profanity', 'REGEX:.*\\b(profanity|inappropriate)\\b.*', 'CRITICAL', true, NOW()),
    (gen_random_uuid(), 'Duplicate Check Rule', 'DUPLICATE_CHECK', 'Checks for duplicate quotes in the system', 'DUPLICATE_CHECK', 'WARNING', true, NOW()),
    (gen_random_uuid(), 'Length Validation Rule', 'CONTENT_VALIDATION', 'Validates quote length is within acceptable limits', 'LENGTH_RANGE:10-500', 'INFO', true, NOW()),
    (gen_random_uuid(), 'Attribution Check Rule', 'CONTENT_VALIDATION', 'Validates that quote has proper attribution', 'ATTRIBUTION_REQUIRED', 'CRITICAL', true, NOW()),
    (gen_random_uuid(), 'Special Characters Rule', 'CONTENT_VALIDATION', 'Checks for excessive special characters', 'REGEX:.*[!@#$%^&*()]{3,}.*', 'WARNING', true, NOW()),
    (gen_random_uuid(), 'Spam Detection Rule', 'CONTENT_VALIDATION', 'Detects potential spam content', 'SPAM_DETECTION', 'CRITICAL', true, NOW())
ON CONFLICT (rule_name) DO NOTHING;

-- Insert sample moderator assignments (for testing)
-- Note: In production, these would be populated by the user management system
INSERT INTO approval_audit_logs (audit_id, approval_request_id, action, performed_by_id, old_value, new_value, timestamp, ip_address, user_agent)
SELECT 
    gen_random_uuid(),
    gen_random_uuid(),
    'SYSTEM_INIT',
    '00000000-0000-0000-0000-000000000000',
    'NULL',
    'System initialized with default queues and rules',
    NOW(),
    '127.0.0.1',
    'System-Internal'
WHERE NOT EXISTS (SELECT 1 FROM approval_audit_logs WHERE performed_by_id = '00000000-0000-0000-0000-000000000000');