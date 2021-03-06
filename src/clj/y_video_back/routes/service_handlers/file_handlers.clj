(ns y-video-back.routes.service-handlers.file-handlers
  (:require
   [y-video-back.db.files :as files]
   [y-video-back.db.resources :as resources]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))

(def file-create
  {:summary "Creates a new file"
   :parameters {:header {:session-id uuid?}
                :body models/file-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (resources/EXISTS? (:resource-id body))
                {:status 500
                 :body {:message "resource not found"}
                 :headers {"session-id" session-id}}
                (if (files/EXISTS-FILEPATH? (:filepath body))
                  {:status 500
                   :body {:message "filepath already in use, unable to create file"}
                   :headers {"session-id" session-id}}
                  {:status 200
                   :body {:message "1 file created"
                          :id (utils/get-id (files/CREATE body))}
                   :headers {"session-id" session-id}})))})



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
                   :body {:message "requested file not found"}
                   :headers {"session-id" session-id}}
                  {:status 200
                   :body res
                   :headers {"session-id" session-id}})))})

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
                 :body {:message "file not found"}
                 :headers {"session-id" session-id}}
                (let [current-file (files/READ id)
                      proposed-file (merge current-file body)
                      same-name-file (first (files/READ-ALL-BY-FILEPATH [(:filepath proposed-file)]))]
                  ; If there is a name-owner collision and the collision is not with self (i.e. file being changed)
                  (if-not (resources/EXISTS? (:resource-id proposed-file))
                    {:status 500
                     :body {:message "resource not found"}
                     :headers {"session-id" session-id}}
                    (if (and (not (nil? same-name-file))
                             (not (= (:id current-file)
                                     (:id same-name-file))))
                      {:status 500
                       :body {:message "unable to update file, filepath likely in use"}
                       :headers {"session-id" session-id}}
                      (let [result (files/UPDATE id body)]
                        (if (= 0 result)
                          {:status 500
                           :body {:message "unable to update file"}
                           :headers {"session-id" session-id}}
                          {:status 200
                           :body {:message (str result " files updated")}
                           :headers {"session-id" session-id}})))))))})

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
                   :body {:message "requested file not found"}
                   :headers {"session-id" session-id}}
                  {:status 200
                   :body {:message (str result " files deleted")}
                   :headers {"session-id" session-id}})))})
