/* UPDATE STATEMENTS */

-- :name update-account :! :n
-- :doc updates account
UPDATE Account
SET email = :email, lastlogin = :lastlogin, name = :name, role = :role,
username = :username
WHERE id = :id

-- :name update-tword :! :n
-- :doc updates tword
UPDATE TWord
SET tword = :tword, src_lang = :src_lang, dest_lang = :dest_lang
WHERE id = :id

-- :name update-collection :! :n
-- :doc updates collection
UPDATE Collection
SET name = :name, published = :published, archived = :archived
WHERE id = :id

-- :name update-course :! :n
-- :doc updates course
UPDATE Course
SET department = :department, catalog_number = :catalog_number,
section_number = :section_number
WHERE id = :id

-- :name update-content :! :n
-- :doc updates content
UPDATE Content
SET id = :id, name = :name, type = :type,
  requester_email = :requester_email, thumbnail = :thumbnail,
  copyrighted = :copyrighted, physical_copy_exists = :physical_copy_exists,
  full_video = :full_video, published = :published,
  allow_definitions = :allow_definitions, allow_notes = :allow_notes,
  allow_captions = :allow_captions, date_validated = :date_validated,
  views = :views, metadata = :metadata
WHERE id = :id

-- :name update-file :! :n
-- :doc updates file
UPDATE File
SET filepath = :filepath, mime = :mime, metadata = :metadata
WHERE id = :id
