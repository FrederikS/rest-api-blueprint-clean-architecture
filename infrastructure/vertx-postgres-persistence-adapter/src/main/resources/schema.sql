CREATE TABLE IF NOT EXISTS categories(
    id uuid PRIMARY KEY default gen_random_uuid(),
    name text,
    slug text,
    parent_id uuid,
    is_visible boolean
);