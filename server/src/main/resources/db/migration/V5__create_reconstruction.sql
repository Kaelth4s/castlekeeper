CREATE TABLE reconstruction (
    id BIGSERIAL PRIMARY KEY,
    castle_id BIGINT NOT NULL REFERENCES castle(id),
    author_id BIGINT NOT NULL REFERENCES author(id),
    reconstruction_year INTEGER
);