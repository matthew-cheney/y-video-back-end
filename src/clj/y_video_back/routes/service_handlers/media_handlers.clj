(ns y-video-back.routes.service-handlers.media-handlers
  (:require
   [y-video-back.db.user-collections-assoc :as user-collections-assoc]
   [y-video-back.db.user-courses-assoc :as user-courses-assoc]
   [y-video-back.db.users :as users]
   [y-video-back.db.files :as files]
   [y-video-back.db.file-keys :as file-keys]
   [y-video-back.models :as models]
   [y-video-back.front-end-models :as fmodels]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]
   [clojure.spec.alpha :as s]
   [y-video-back.db.core :as db]))

;; TODO - check if user has permission to stream requested file

(def get-file-key ;; Non-functional
  {:summary "Gets volatile url for streaming specified media file"
   :parameters {
                :header {:session-id uuid?}
                :path {:file-id uuid?}}
   ;:responses {200 {:body [models/user]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [file-id]} :path} :parameters}]
              (let [user-id (ru/token-to-user-id session-id)
                    file-key (file-keys/CREATE {:file-id file-id
                                                :user-id user-id})]
                {:status 200
                 :body {:file-key file-key}}))})

(def stream-media ;; Non-functional
  {:summary "Stream media referenced by file-key"
   :parameters {
                ;:header {:session-id uuid?}
                :path {:file-key uuid?}}
   ;:responses {200 {:body [models/user]}}
   ;:handler (fn [{{{:keys [session-id]} :header {:keys [file-id]} :path} :parameters}])
   :handler (fn [{{{:keys [file-key]} :path} :parameters}]
              (let [file-key-res (file-keys/READ file-key)]
                (ring.util.response/file-response (utils/file-id-to-path (:file-id file-key-res)))))})
