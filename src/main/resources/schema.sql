create table if not exists localizations (
    id bigint generated always as identity primary key,
    city varchar(255) not null,
    region varchar(255),
    country varchar(255) not null,
    longitude double precision,
    latitude double precision,
    sort_order bigint not null
    );

CREATE TABLE weather (
                         id BIGSERIAL PRIMARY KEY,
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

CREATE TABLE forecast (
                          id BIGSERIAL PRIMARY KEY,

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

CREATE INDEX idx_forecast_localization_time
    ON forecast(localization_id, forecast_time);

CREATE INDEX idx_forecast_fetched_at
    ON forecast(localization_id, fetched_at DESC);

CREATE INDEX idx_weather_localization_id
    ON weather(localization_id);

CREATE INDEX idx_weather_localization_fetched_at
    ON weather(localization_id, fetched_at DESC);

ALTER TABLE localizations
    ADD CONSTRAINT uk_my_table_list_id_order_by UNIQUE (sort_order);