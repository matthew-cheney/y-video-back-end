(ns y-video-back.routes.service-handlers.file-handlers
  (:require
   [y-video-back.config :refer [env]]
   [y-video-back.db.files :as files]
   [y-video-back.db.resources :as resources]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]
   [ring.swagger.upload :as upload]))
(def file-create
  {:summary "Creates a new file. MUST INCLUDE FILE AS UPLOAD."
   :parameters {:header {:session-id uuid?}
                :body (dissoc models/file-without-id :filepath)}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [req]
              ;(println "req=" req)
              (let [session-id (get-in req [:parameters :header :session-id])
                    body (get-in req [:parameters :body])
                    file-params (get-in req [:params "file"])
                    file-name (utils/get-filename (:filename file-params))]
                (if (nil? file-params)
                  {:status 400
                   :body {:message "missing file to upload"}}
                  (if-not (resources/EXISTS? (:resource-id body))
                    {:status 500
                     :body {:message "resource not found"}}
                    (do
                      (clojure.java.io/copy (:tempfile file-params)
                                            (clojure.java.io/file (str (-> env :FILES :media-url) file-name)))
                      {:status 200
                       :body {:message "1 file created"
                              :id (utils/get-id (files/CREATE (assoc body :filepath file-name)))}})))))})



(def file-get-by-id
  {:summary "Retrieves specified file"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/file}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (let [res (files/READ id)]
                (if (nil? res)
                  {:status 404
                   :body {:message "requested file not found"}}
                  {:status 200
                   :body res})))})

(def file-update
  {:summary "Updates specified file"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/file}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (files/EXISTS? id)
                {:status 404
                 :body {:message "file not found"}}
                (let [current-file (files/READ id)
                      proposed-file (merge current-file body)
                      same-name-file (first (files/READ-ALL-BY-FILEPATH [(:filepath proposed-file)]))]
                  ; If there is a name-owner collision and the collision is not with self (i.e. file being changed)
                  (if-not (resources/EXISTS? (:resource-id proposed-file))
                    {:status 500
                     :body {:message "resource not found"}}
                    (if (and (not (nil? same-name-file))
                             (not (= (:id current-file)
                                     (:id same-name-file))))
                      {:status 500
                       :body {:message "unable to update file, filepath likely in use"}}
                      (let [result (files/UPDATE id body)]
                        (if (= 0 result)
                          {:status 500
                           :body {:message "unable to update file"}}
                          {:status 200
                           :body {:message (str result " files updated")}})))))))})

(def file-delete
  {:summary "Deletes specified file"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (let [result (files/DELETE id)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested file not found"}}
                  {:status 200
                   :body {:message (str result " files deleted")}})))})
