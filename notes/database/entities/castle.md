| Column         | Type      | Constraints |
| -------------- | --------- | ----------- |
| id             | bigserial | PK          |
| name           | text      |             |
| description    | text      |             |
| author_id      | bigint    | FK          |
| built_year     | integer   |             |
| destroyed_year | integer   |             |
| height_m       | numeric   |             |
| material_id    | bigint    | FK          |