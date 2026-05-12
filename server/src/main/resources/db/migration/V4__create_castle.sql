CREATE TABLE castle (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    author_id BIGINT REFERENCES author(id),
    built_year INTEGER,
    destroyed_year INTEGER,
    height_m NUMERIC,
    material_id BIGINT REFERENCES material(id)
);