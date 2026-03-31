CREATE TABLE IF NOT EXISTS localizations (
                                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                             city VARCHAR(255) NOT NULL,
    region VARCHAR(255),
    country VARCHAR(255) NOT NULL,
    longitude DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    sort_order BIGINT NOT NULL,
    CONSTRAINT uk_localizations_sort_order UNIQUE (sort_order)
    );

CREATE TABLE IF NOT EXISTS weather (
                                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                       description VARCHAR(255),
    temperature DOUBLE PRECISION,
    feels_like DOUBLE PRECISION,
    pressure DOUBLE PRECISION,
    humidity INTEGER,
    wind_speed DOUBLE PRECISION,
    wind_deg DOUBLE PRECISION,
    clouds_all DOUBLE PRECISION,
    provider_timestamp TIMESTAMP,
    fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    localization_id BIGINT NOT NULL,
    CONSTRAINT fk_weather_localization
    FOREIGN KEY (localization_id)
    REFERENCES localizations(id)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS forecast (
                                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        localization_id BIGINT NOT NULL,
                                        forecast_time TIMESTAMP NOT NULL,
                                        temperature DOUBLE PRECISION,
                                        feels_like DOUBLE PRECISION,
                                        pressure DOUBLE PRECISION,
                                        humidity INTEGER,
                                        wind_speed DOUBLE PRECISION,
                                        wind_deg DOUBLE PRECISION,
                                        description VARCHAR(255),
    precipitation_probability DOUBLE PRECISION,
    rain_volume DOUBLE PRECISION,
    snow_volume DOUBLE PRECISION,
    clouds_all DOUBLE PRECISION,
    fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_forecast_localization
    FOREIGN KEY (localization_id)
    REFERENCES localizations(id)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_forecast_localization_time
    ON forecast(localization_id, forecast_time);

CREATE INDEX IF NOT EXISTS idx_forecast_fetched_at
    ON forecast(localization_id, fetched_at DESC);

CREATE INDEX IF NOT EXISTS idx_weather_localization_id
    ON weather(localization_id);

CREATE INDEX IF NOT EXISTS idx_weather_localization_fetched_at
    ON weather(localization_id, fetched_at DESC);