```mehrmaid
graph TD
    reconstruction ==>|"n:1<br/>castle_id (FK)"| castle
    reconstruction ==>|"n:1<br/>author_id (FK)"| author
    author ==>|"n:1<br/>author_type_id (FK)"| author_type
    castle ==>|"n:1<br/>material_id (FK)"| material
    castle ==>|"n:1<br/>author_id (FK)"| author

    reconstruction("![[reconstruction]]")
    author("![[author]]")
    author_type("![[author_type]]")
    castle("![[castle]]")
    material("![[material]]")
```
