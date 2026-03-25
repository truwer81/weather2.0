create table if not exists localizations (
    id bigint generated always as identity primary key,
    city varchar(255) not null,
    region varchar(255),
    country varchar(255) not null,
    longitude double precision,
    latitude double precision
    );