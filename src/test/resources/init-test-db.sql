-- Test database initialization script
-- This script creates the basic tables needed for testing

-- Create approval_queues table
CREATE TABLE IF NOT EXISTS approval_queues (
    queue_id UUID PRIMARY KEY,
    queue_name VARCHAR(255) NOT NULL UNIQUE,
    max_capacity INTEGER NOT NULL,
    current_size INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_processed_at TIMESTAMPTZ
);

-- Create approval_requests table
CREATE TABLE IF NOT EXISTS approval_requests (
    approval_request_id UUID PRIMARY KEY,
    quote_id UUID NOT NULL,
    submitted_by_id UUID NOT NULL,
    priority VARCHAR(50) NOT NULL,
    assigned_moderator_id UUID,
    status VARCHAR(50) NOT NULL,
    submitted_at TIMESTAMPTZ NOT NULL,
    assigned_at TIMESTAMPTZ,
    deadline TIMESTAMPTZ,
    queue_id UUID,
    FOREIGN KEY (queue_id) REFERENCES approval_queues(queue_id)
);

-- Create approval_decisions table
CREATE TABLE IF NOT EXISTS approval_decisions (
    decision_id UUID PRIMARY KEY,
    approval_request_id UUID NOT NULL,
    moderator_id UUID NOT NULL,
    decision VARCHAR(50) NOT NULL,
    reason TEXT,
    comments TEXT,
    decision_timestamp TIMESTAMPTZ NOT NULL,
    processing_time_ms BIGINT,
    FOREIGN KEY (approval_request_id) REFERENCES approval_requests(approval_request_id)
);

-- Create approval_audit_logs table
CREATE TABLE IF NOT EXISTS approval_audit_logs (
    audit_id UUID PRIMARY KEY,
    approval_request_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by_id UUID NOT NULL,
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMPTZ NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    FOREIGN KEY (approval_request_id) REFERENCES approval_requests(approval_request_id)
);

-- Create moderation_rules table
CREATE TABLE IF NOT EXISTS moderation_rules (
    rule_id UUID PRIMARY KEY,
    rule_name VARCHAR(255) NOT NULL UNIQUE,
    rule_type VARCHAR(100) NOT NULL,
    description TEXT,
    rule_expression TEXT NOT NULL,
    severity VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create approval_request_rule_executions table
CREATE TABLE IF NOT EXISTS approval_request_rule_executions (
    approval_request_id UUID NOT NULL,
    rule_id UUID NOT NULL,
    execution_timestamp TIMESTAMPTZ NOT NULL,
    result VARCHAR(50) NOT NULL,
    details TEXT,
    PRIMARY KEY (approval_request_id, rule_id),
    FOREIGN KEY (approval_request_id) REFERENCES approval_requests(approval_request_id),
    FOREIGN KEY (rule_id) REFERENCES moderation_rules(rule_id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_approval_requests_status ON approval_requests(status);
CREATE INDEX IF NOT EXISTS idx_approval_requests_quote_id ON approval_requests(quote_id);
CREATE INDEX IF NOT EXISTS idx_approval_requests_moderator ON approval_requests(assigned_moderator_id);
CREATE INDEX IF NOT EXISTS idx_approval_decisions_request_id ON approval_decisions(approval_request_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_request_id ON approval_audit_logs(approval_request_id);
CREATE INDEX IF NOT EXISTS idx_queues_active ON approval_queues(is_active);

-- Insert test data
INSERT INTO approval_queues (queue_id, queue_name, max_capacity, current_size, is_active) VALUES 
('550e8400-e29b-41d4-a716-446655440000', 'General Queue', 1000, 0, true),
('550e8400-e29b-41d4-a716-446655440001', 'High Priority Queue', 500, 0, true),
('550e8400-e29b-41d4-a716-446655440002', 'Urgent Queue', 100, 0, true)
ON CONFLICT (queue_name) DO NOTHING;

INSERT INTO moderation_rules (rule_id, rule_name, rule_type, description, rule_expression, severity, is_active) VALUES 
('660e8400-e29b-41d4-a716-446655440000', 'Profanity Check Rule', 'CONTENT_VALIDATION', 'Checks for profanity in quotes', '.*\\b(badword1|badword2)\\b.*', 'CRITICAL', true),
('660e8400-e29b-41d4-a716-446655440001', 'Duplicate Check Rule', 'DUPLICATE_CHECK', 'Checks for duplicate quotes', 'duplicate_check_expression', 'WARNING', true),
('660e8400-e29b-41d4-a716-446655440002', 'Length Validation Rule', 'CONTENT_VALIDATION', 'Validates quote length', '^(.{0,9}|.{501,})$', 'INFO', true)
ON CONFLICT (rule_name) DO NOTHING;