ALTER TABLE localizations
DROP CONSTRAINT IF EXISTS fk_localizations_owner_user;

ALTER TABLE localizations
    ADD CONSTRAINT fk_localizations_owner_user
        FOREIGN KEY (owner_user_id)
            REFERENCES app_users(id)
            ON DELETE CASCADE;