ALTER TABLE users
    ADD COLUMN failed_attempt     INT     NOT NULL DEFAULT 0,
    ADD COLUMN account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN lock_time          TIMESTAMP;