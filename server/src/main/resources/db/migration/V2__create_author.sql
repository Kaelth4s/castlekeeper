CREATE TABLE author (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    author_type_id BIGINT NOT NULL REFERENCES author_type(id)
);