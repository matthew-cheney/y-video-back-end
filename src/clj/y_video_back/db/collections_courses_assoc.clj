(ns y-video-back.db.collections-courses-assoc
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :collection-courses-assoc))
(def READ  (partial db/READ :collection-courses-assoc-undeleted))
(def READ-ALL  (partial db/READ :collection-courses-assoc))
(def UPDATE (partial db/UPDATE :collection-courses-assoc))
(def DELETE (partial db/mark-deleted :collection-courses-assoc))
(def CLONE (partial db/CLONE :collection-courses-assoc))
(def PERMANENT-DELETE (partial db/DELETE :collection-courses-assoc))
(def READ-BY-IDS "[column-vals & select-field-keys]\ncolumn-vals must be a collection containing collection-id then course-id. select-field-keys, if given, must be a collection containing keywords representing columns to return from db." (partial db/read-where-and :collection-courses-assoc-undeleted [:collection-id :course-id]))
(def DELETE-BY-IDS "[column-vals]\ncolumn-vals must be a collection containing collection-id then course-id." (partial db/delete-where-and :collection-courses-assoc-undeleted [:collection-id :course-id]))
(def READ-COURSES-BY-COLLECTION (partial db/read-all-where :courses-by-collection :collection-id))
(def READ-COLLECTIONS-BY-COURSE (partial db/read-all-where :collections-by-course :course-id))
