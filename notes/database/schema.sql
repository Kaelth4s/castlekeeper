CREATE TABLE IF NOT EXISTS "author_type" (
	"id" BIGSERIAL,
	"name" TEXT NOT NULL UNIQUE,
	"description" TEXT,
	PRIMARY KEY("id")
);




CREATE TABLE IF NOT EXISTS "author" (
	"id" BIGSERIAL,
	"name" TEXT NOT NULL,
	"author_type_id" BIGINT NOT NULL,
	PRIMARY KEY("id")
);




CREATE TABLE IF NOT EXISTS "material" (
	"id" BIGSERIAL,
	"name" TEXT NOT NULL UNIQUE,
	PRIMARY KEY("id")
);




CREATE TABLE IF NOT EXISTS "castle" (
	"id" BIGSERIAL,
	"name" TEXT NOT NULL,
	"description" TEXT,
	"author_id" BIGINT,
	"built_year" INTEGER,
	"destroyed_year" INTEGER,
	"height_m" NUMERIC,
	"material_id" BIGINT,
	PRIMARY KEY("id")
);




CREATE TABLE IF NOT EXISTS "reconstruction" (
	"id" BIGSERIAL,
	"castle_id" BIGINT NOT NULL,
	"author_id" BIGINT NOT NULL,
	"reconstruction_year" INTEGER,
	PRIMARY KEY("id")
);



ALTER TABLE "castle"
ADD FOREIGN KEY("author_id") REFERENCES "author"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "castle"
ADD FOREIGN KEY("material_id") REFERENCES "material"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "reconstruction"
ADD FOREIGN KEY("castle_id") REFERENCES "castle"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "reconstruction"
ADD FOREIGN KEY("author_id") REFERENCES "author"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE "author"
ADD FOREIGN KEY("author_type_id") REFERENCES "author_type"("id")
ON UPDATE NO ACTION ON DELETE NO ACTION;