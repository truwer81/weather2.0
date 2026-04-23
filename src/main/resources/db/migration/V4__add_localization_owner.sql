ALTER TABLE localizations
    ADD COLUMN owner_user_id BIGINT;

ALTER TABLE localizations
    ADD CONSTRAINT fk_localizations_owner_user
        FOREIGN KEY (owner_user_id) REFERENCES app_users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_localizations_owner_user_id
    ON localizations(owner_user_id);
