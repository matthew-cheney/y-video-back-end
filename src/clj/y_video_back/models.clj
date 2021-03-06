(ns y-video-back.models
  (:require [schema.core :as sch]
            [spec-tools.core :as st]
            [clojure.spec.alpha :as s]))

(defn add-namespace
  "Converts all keywords to namespace-keywords, returns vector of keywords"
  [namespace m]
  (into []
    (map (fn [val]
            (keyword
              namespace
              (clojure.string/replace
                (str
                  (get val 0))
                ":"
                "")))
      m)))

(defn to-uuid
  [text-in]
  (java.util.UUID/fromString text-in))


(defn nuuid?
  "Returns true if val is uuid or nil"
  [val]
  (or (nil? val)
      (uuid? val)
      (uuid? (to-uuid val))))

(def echo-patch
  {:echo string?})

(def user-without-id
  {:email string?
   :last-login string?
   :account-name string?
   :account-type int?
   :username string?})

(def user
  (into user-without-id {:id uuid?}))

(def user-without-id-ns-params  ; Not in use
  (add-namespace "user" {:variable string?}))

(def word-without-id-or-user-id
  {:word string?
   :src-lang string?
   :dest-lang string?})

(def word-without-id
  (into word-without-id-or-user-id {:user-id uuid?}))

(def word
  (into word-without-id {:id uuid?}))

(def collection-without-id-or-owner
  {:collection-name string?
   :published boolean?
   :archived boolean?})

(def collection-without-id
  (into collection-without-id-or-owner {:owner uuid?}))

(def collection
  (into collection-without-id {:id uuid?}))

(def collection-read-from-db
  (-> collection-without-id-or-owner
      (into {:id uuid?})
      (into {:owner user})))

(def course-without-id
  {:department string?
   :catalog-number string?
   :section-number string?})

(def course
  (into course-without-id {:id uuid?}))

(def resource-without-id
  {:resource-name string?
   :resource-type string?
   :requester-email string?
   :copyrighted boolean?
   :physical-copy-exists boolean?
   :full-video boolean?
   :published boolean?
   :date-validated string?
   :views int?
   :all-file-versions string?
   :metadata string?})

(def resource
  (into resource-without-id {:id uuid?}))

(def content-without-any-ids
  {:title string?
   :content-type string?
   :url string?
   :description string?
   :tags string?
   :annotations string?
   :thumbnail string?
   :allow-definitions boolean?
   :allow-notes boolean?
   :allow-captions boolean?
   :views integer?
   :file-version string?})

(def content-without-id
  (into content-without-any-ids {:resource-id uuid?
                                 :collection-id uuid?}))

(def content
  (into content-without-id {:id uuid?}))

(def subtitle-without-any-ids
  {:title string?
   :language string?
   :content string?})

(def subtitle-without-id
  (into subtitle-without-any-ids {:resource-id uuid?}))

(def subtitle
  (into subtitle-without-id {:id uuid?}))

(def file-without-any-ids
  {:filepath string?
   :file-version string?
   :mime string?
   :metadata string?})

(def file-without-id
  (into file-without-any-ids {:resource-id uuid?}))

(def file
  (into file-without-id {:id uuid?}))

(def user-collections-assoc-without-any-ids
  {:account-role int?})

(def user-collections-assoc-without-id
  (into user-collections-assoc-without-any-ids {:user-id uuid? :collection-id uuid?}))

(def user-collections-assoc
  (into user-collections-assoc-without-id {:id uuid?}))

(def user-courses-assoc-without-any-ids
  {:account-role int?})

(def user-courses-assoc-without-id
  (into user-courses-assoc-without-any-ids {:user-id uuid? :course-id uuid?}))

(def user-courses-assoc
  (into user-courses-assoc-without-id {:id uuid?}))


;(def collection-resources-assoc-without-any-ids
;  {})

;(def collection-resources-assoc-without-id
;  (into collection-resources-assoc-without-any-ids {:collection-id uuid? :resource-id uuid?}))

;(def collection-resources-assoc
;  (into collection-resources-assoc-without-id {:id uuid?}))

(def collection-courses-assoc-without-any-ids
  {})

(def collection-courses-assoc-without-id
  (into collection-courses-assoc-without-any-ids {:collection-id uuid? :course-id uuid?}))

(def collection-courses-assoc
  (into collection-courses-assoc-without-id {:id uuid?}))

(def content-subtitles-assoc-without-any-ids
  {})

(def content-subtitles-assoc-without-id
  (into content-subtitles-assoc-without-any-ids {:content-id uuid? :subtitle-id uuid?}))

(def content-subtitles-assoc
  (into content-subtitles-assoc-without-id {:id uuid?}))


;(def resource-files-assoc-without-any-ids
;  {})

;(def resource-files-assoc-without-id
;  (into resource-files-assoc-without-any-ids {:resource-id uuid? :file-id uuid?}))

;(def resource-files-assoc
;  (into resource-files-assoc-without-id {:id uuid?}))
