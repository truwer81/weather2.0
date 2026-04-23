ALTER TABLE localizations
    DROP CONSTRAINT IF EXISTS uk_localizations_sort_order;

CREATE UNIQUE INDEX IF NOT EXISTS uk_localizations_shared_sort_order
    ON localizations(sort_order)
    WHERE owner_user_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_localizations_private_owner_sort_order
    ON localizations(owner_user_id, sort_order)
    WHERE owner_user_id IS NOT NULL;
