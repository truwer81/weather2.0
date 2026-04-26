ALTER TABLE localizations
    RENAME TO locations;

ALTER TABLE locations
    RENAME COLUMN city TO name;

ALTER TABLE weather
    RENAME COLUMN localization_id TO location_id;

ALTER TABLE forecast
    RENAME COLUMN localization_id TO location_id;

ALTER TABLE locations
    RENAME CONSTRAINT localizations_pkey TO locations_pkey;

ALTER TABLE locations
    RENAME CONSTRAINT fk_localizations_owner_user TO fk_locations_owner_user;

ALTER TABLE weather
    RENAME CONSTRAINT fk_weather_localization TO fk_weather_location;

ALTER TABLE forecast
    RENAME CONSTRAINT fk_forecast_localization TO fk_forecast_location;

ALTER INDEX uk_localizations_shared_sort_order
    RENAME TO uk_locations_shared_sort_order;

ALTER INDEX uk_localizations_private_owner_sort_order
    RENAME TO uk_locations_private_owner_sort_order;

ALTER INDEX idx_localizations_owner_user_id
    RENAME TO idx_locations_owner_user_id;

ALTER INDEX idx_weather_localization_id
    RENAME TO idx_weather_location_id;

ALTER INDEX idx_weather_localization_fetched_at
    RENAME TO idx_weather_location_fetched_at;

ALTER INDEX idx_forecast_localization_time
    RENAME TO idx_forecast_location_time;

ALTER INDEX idx_forecast_fetched_at
    RENAME TO idx_forecast_location_fetched_at;

ALTER SEQUENCE localizations_id_seq
    RENAME TO locations_id_seq;
