(ns y-video-back.db.resources
  (:require [y-video-back.db.core :as db]))

(def CREATE (partial db/CREATE :resources))
(def READ  (partial db/READ :resources-undeleted))
(def READ-ALL  (partial db/READ :resources))
(def UPDATE (partial db/UPDATE :resources))
(def DELETE (partial db/mark-deleted :resources))
(def CLONE (partial db/CLONE :resources))
(def PERMANENT-DELETE (partial db/DELETE :resources))
(defn EXISTS? [id] (not (nil? (db/READ :resources-undeleted id))))
(defn NAME-TAKEN? [resource-name] (not (empty? (db/read-where-and :resources-undeleted [:resource-name] [resource-name]))))
(def READ-ALL-BY-NAME (partial db/read-where-and :resources-undeleted [:resource-name]))
(def COLLECTIONS-BY-RESOURCE (partial db/read-all-where :collections-by-resource :resource-id))