CREATE TABLE assignment_preview_jobs (
    id UUID PRIMARY KEY,
    requester_account_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    request_command_json JSONB NOT NULL,
    result_preview_json JSONB,
    failure_code VARCHAR(64),
    failure_message VARCHAR(255),
    model VARCHAR(64) NOT NULL,
    repair_attempted BOOLEAN NOT NULL DEFAULT FALSE,
    queue_wait_ms BIGINT,
    initial_ai_elapsed_ms BIGINT,
    repair_ai_elapsed_ms BIGINT,
    execution_elapsed_ms BIGINT,
    total_elapsed_ms BIGINT,
    submitted_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_assignment_preview_jobs_requester_account
        FOREIGN KEY (requester_account_id) REFERENCES identity_accounts(id)
);

CREATE INDEX idx_assignment_preview_jobs_requester_status
    ON assignment_preview_jobs (requester_account_id, status);

CREATE INDEX idx_assignment_preview_jobs_submitted_at
    ON assignment_preview_jobs (submitted_at);

CREATE UNIQUE INDEX uq_assignment_preview_jobs_requester_active
    ON assignment_preview_jobs (requester_account_id)
    WHERE status IN ('QUEUED', 'RUNNING');
