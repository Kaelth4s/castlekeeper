сервер располагается на домене

castlekeeper.kaelth4s.ru

localhost:8080

API располагается в:

localhost:8080/api/
castlekeeper.kaelth4s.ru/api

## CRUD

Замки:
- GET (все замки): /api/castles
- GET (случайный замок): /api/castles/random
- GET (по ID): /api/castles/{id}
- GET (по name): /api/castles?name=
- POST (добавить замок): /api/castles
- DELETE (удалить замок по ID): /api/castles/{id}
- PUT (редактировать замок по ID): /api/castles/{id}

Авторы:
- GET (все авторы): /api/authors
- GET (по ID): /api/authors/{id}
- POST (добавить автора): /api/authors
- DELETE (удалить автора по ID): /api/authors/{id}
- PUT (редактировать автора по ID): /api/authors/{id}

Материалы
- GET (все материалы): /api/materials
- GET (по ID): /api/materials/{id}
- POST (добавить материал): /api/materials
- DELETE (удалить материал по ID): /api/materials/{id}
- PUT (редактировать материал по ID): /api/materials/{id}