CREATE TABLE content_reports (
    id UUID PRIMARY KEY,
    reporter_user_id BIGINT NOT NULL,
    discussion_id UUID,
    comment_id UUID,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    reviewed_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_content_reports_reporter
        FOREIGN KEY (reporter_user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_content_reports_discussion
        FOREIGN KEY (discussion_id)
        REFERENCES discussions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_content_reports_comment
        FOREIGN KEY (comment_id)
        REFERENCES comments(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_content_reports_target
        CHECK ((discussion_id IS NOT NULL AND comment_id IS NULL)
            OR (discussion_id IS NULL AND comment_id IS NOT NULL)),

    CONSTRAINT chk_content_reports_status
        CHECK (status IN ('PENDING', 'REVIEWED', 'DISMISSED'))
);

CREATE INDEX idx_content_reports_status
    ON content_reports(status);

CREATE INDEX idx_content_reports_created_at
    ON content_reports(created_at);

CREATE UNIQUE INDEX uq_content_reports_discussion_reporter
    ON content_reports(reporter_user_id, discussion_id)
    WHERE discussion_id IS NOT NULL;

CREATE UNIQUE INDEX uq_content_reports_comment_reporter
    ON content_reports(reporter_user_id, comment_id)
    WHERE comment_id IS NOT NULL;
