-- This DB implements UUID ids on every table, constraints for composite keys, soft-deletes and "updated" for data audit

-- DROP EXTENSION IF EXISTS pgcrypto CASCADE;
-- CREATE EXTENSION pgcrypto;
DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,email TEXT UNIQUE
   ,last_login TEXT
   ,account_name TEXT
   ,account_type INTEGER
   ,username TEXT
);
COMMENT ON TABLE users IS 'User-accounts matching netid';

DROP TABLE IF EXISTS words CASCADE;
CREATE TABLE words (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,user_id UUID REFERENCES users(id)
   ,word TEXT
   ,src_lang TEXT
   ,dest_lang TEXT
   , CONSTRAINT no_duplicate_user_words UNIQUE (user_id, word, src_lang, dest_lang)
);
COMMENT ON TABLE words IS 'Vocab words with source and destination language codes';


DROP TABLE IF EXISTS collections CASCADE;
CREATE TABLE collections (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,collection_name TEXT -- Because name is a reserved word
   ,owner UUID REFERENCES users(id)
   ,published BOOLEAN
   ,archived BOOLEAN
   , CONSTRAINT no_duplicate_owner_names UNIQUE (owner, collection_name)
);
COMMENT ON TABLE collections IS 'Collections of content/resources';

DROP TABLE IF EXISTS courses CASCADE;
CREATE TABLE courses (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,department TEXT -- should this be a foreign key?
   ,catalog_number TEXT
   ,section_number TEXT
   , CONSTRAINT no_duplicate_courses UNIQUE (department, catalog_number, section_number)
);
COMMENT ON TABLE courses IS 'Courses (or classes) at BYU';

DROP TABLE IF EXISTS contents CASCADE;
CREATE TABLE contents (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,content_name TEXT
   ,content_type TEXT -- type is a reserved word
   ,requester_email TEXT
   ,thumbnail TEXT
   ,copyrighted BOOLEAN
   ,physical_copy_exists BOOLEAN
   ,full_video BOOLEAN
   ,published BOOLEAN
   ,allow_definitions BOOLEAN
   ,allow_notes BOOLEAN
   ,allow_captions BOOLEAN
   ,date_validated TEXT
   ,views INTEGER
   ,metadata TEXT
);
COMMENT ON TABLE contents IS 'Contained within collections, hold media in files table';

DROP TABLE IF EXISTS files CASCADE;
CREATE TABLE files (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,filepath TEXT
   ,mime TEXT
   ,metadata TEXT
   , CONSTRAINT no_duplicate_filepaths UNIQUE (filepath)
);
COMMENT ON TABLE files IS 'Files represent media (i.e. videos) with path to file and metadata';

DROP TABLE IF EXISTS annotations CASCADE;
CREATE TABLE annotations (
    id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
    ,deleted TIMESTAMP DEFAULT NULL
    ,updated TIMESTAMP DEFAULT NULL
    ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ,content_id UUID REFERENCES contents(id)
    ,collection_id UUID REFERENCES collections(id)
    ,metadata TEXT
    , CONSTRAINT no_duplicate_annotations UNIQUE (content_id, collection_id)
);
COMMENT ON TABLE annotations IS 'Contains annotations to be applied over contents';

DROP TABLE IF EXISTS user_collections_assoc CASCADE;
CREATE TABLE user_collections_assoc (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,user_id UUID REFERENCES users(id) ON DELETE CASCADE
   ,collection_id UUID REFERENCES collections(id) ON DELETE CASCADE
   ,account_role INTEGER
   , CONSTRAINT no_duplicate_user_collections UNIQUE (user_id, collection_id)
);
COMMENT ON TABLE user_collections_assoc IS 'Many-to-many table connecting users and collections, incl. user roles in collections';

DROP TABLE IF EXISTS user_courses_assoc CASCADE;
CREATE TABLE user_courses_assoc (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,user_id UUID REFERENCES users(id) ON DELETE CASCADE
   ,course_id UUID REFERENCES courses(id) ON DELETE CASCADE
   ,account_role INTEGER
   , CONSTRAINT no_duplicate_user_courses UNIQUE (user_id, course_id)
);
COMMENT ON TABLE user_courses_assoc IS 'Many-to-many table connecting users and courses, incl. user roles in courses';


DROP TABLE IF EXISTS collection_courses_assoc CASCADE;
CREATE TABLE collection_courses_assoc (
   id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,collection_id UUID REFERENCES collections(id)
   ,course_id UUID REFERENCES courses(id)
   , CONSTRAINT no_duplicate_course_collections UNIQUE (course_id, collection_id)
);
COMMENT ON TABLE collection_courses_assoc IS 'Many-to-many table connecting collections and courses';

DROP TABLE IF EXISTS collection_contents_assoc CASCADE;



DROP TABLE IF EXISTS content_files_assoc CASCADE;
CREATE TABLE content_files_assoc (
   id UUID NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY
   ,deleted TIMESTAMP DEFAULT NULL
   ,updated TIMESTAMP DEFAULT NULL
   ,created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ,content_id UUID REFERENCES contents(id)
   ,file_id UUID REFERENCES files(id)
   , CONSTRAINT no_duplicate_content_files UNIQUE (content_id, file_id)
);
COMMENT ON TABLE content_files_assoc IS 'Many-to-many table connecting contents and files';

-------------------------------
-- Auto-update updated --
-------------------------------
CREATE OR REPLACE FUNCTION modified_timestamp() RETURNS TRIGGER
LANGUAGE plpgsql
AS
$$
BEGIN
   NEW.updated = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$;

DO $$
DECLARE
   t text;
BEGIN
   FOR t IN
      SELECT tabs.table_name, table_type FROM information_schema.columns AS cols
      JOIN information_schema.tables AS tabs
      ON tabs.table_name = cols.table_name
      WHERE column_name = 'updated' AND tabs.table_type = 'BASE TABLE'
      LOOP
      EXECUTE format('DROP TRIGGER IF EXISTS updated_%I on %I',
      t,t);
      EXECUTE format('CREATE TRIGGER updated_%I
      BEFORE UPDATE ON %I
      FOR EACH ROW EXECUTE PROCEDURE modified_timestamp()',
      t,t);
   END LOOP;
END;

---------------------
-- UNDELETED VIEWS --
---------------------
$$ LANGUAGE plpgsql;

DO $$
DECLARE
   t text;
BEGIN
   FOR t IN
     SELECT tabs.table_name, table_type FROM information_schema.columns AS cols
     JOIN information_schema.tables AS tabs
     ON tabs.table_name = cols.table_name
     WHERE column_name = 'deleted' AND tabs.table_type = 'BASE TABLE'
     LOOP
     EXECUTE format('DROP VIEW IF EXISTS %I_undeleted CASCADE',
     t);
     EXECUTE format('CREATE VIEW %I_undeleted
     AS SELECT %I.* FROM %I where %I.deleted IS NULL',
     t,t,t,t);
   END LOOP;
END;
$$ LANGUAGE plpgsql;

---------------------
-- DEPENDENT VIEWS --
---------------------

-- These will be views which depend upon other views, such as *_undeleted

DROP VIEW IF EXISTS users_by_collection;
CREATE VIEW users_by_collection AS
    SELECT users_undeleted.*, uca.account_role, uca.collection_id
    FROM users_undeleted JOIN user_collections_assoc_undeleted AS uca
    ON users_undeleted.id = uca.user_id;

DROP VIEW IF EXISTS collections_by_user;
CREATE VIEW collections_by_user AS
    SELECT collections_undeleted.*, uca.account_role, uca.user_id
    FROM collections_undeleted JOIN user_collections_assoc_undeleted AS uca
    ON collections_undeleted.id = uca.collection_id;

DROP VIEW IF EXISTS users_by_course;
CREATE VIEW users_by_course AS
    SELECT users_undeleted.*, uca.account_role, uca.course_id
    FROM users_undeleted JOIN user_courses_assoc_undeleted AS uca
    ON users_undeleted.id = uca.user_id;

DROP VIEW IF EXISTS courses_by_user;
CREATE VIEW courses_by_user AS
    SELECT courses_undeleted.*, uca.account_role, uca.user_id
    FROM courses_undeleted JOIN user_courses_assoc_undeleted AS uca
    ON courses_undeleted.id = uca.course_id;


DROP VIEW IF EXISTS collections_by_content;
CREATE VIEW collections_by_content AS
    SELECT collections_undeleted.*, cca.content_id
    FROM collections_undeleted JOIN annotations_undeleted AS cca
    ON collections_undeleted.id = cca.collection_id;

DROP VIEW IF EXISTS contents_by_collection;
CREATE VIEW contents_by_collection AS
    SELECT contents_undeleted.*, cca.collection_id
    FROM contents_undeleted JOIN annotations_undeleted AS cca
    ON contents_undeleted.id = cca.content_id;

DROP VIEW IF EXISTS collections_by_course;
CREATE VIEW collections_by_course AS
    SELECT collections_undeleted.*, cca.course_id
    FROM collections_undeleted JOIN collection_courses_assoc_undeleted AS cca
    ON collections_undeleted.id = cca.collection_id;

DROP VIEW IF EXISTS courses_by_collection;
CREATE VIEW courses_by_collection AS
    SELECT courses_undeleted.*, cca.collection_id
    FROM courses_undeleted JOIN collection_courses_assoc_undeleted AS cca
    ON courses_undeleted.id = cca.course_id;

DROP VIEW IF EXISTS contents_by_file;
CREATE VIEW contents_by_file AS
    SELECT contents_undeleted.*, cca.file_id
    FROM contents_undeleted JOIN content_files_assoc_undeleted AS cca
    ON contents_undeleted.id = cca.content_id;

DROP VIEW IF EXISTS files_by_content;
CREATE VIEW files_by_content AS
    SELECT files_undeleted.*, cca.content_id
    FROM files_undeleted JOIN content_files_assoc_undeleted AS cca
    ON files_undeleted.id = cca.file_id;

DROP VIEW IF EXISTS collections_by_users_via_courses;
CREATE VIEW collections_by_users_via_courses AS
    SELECT collections_undeleted.*, uca.user_id
    FROM users_undeleted
    JOIN user_courses_assoc AS uca
    ON users_undeleted.id = uca.user_id
    JOIN collection_courses_assoc AS cca
    ON uca.course_id = cca.course_id
    JOIN collections_undeleted
    ON cca.collection_id = collections_undeleted.id;
