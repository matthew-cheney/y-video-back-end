(ns y-video-back.routes.service_handlers.file_handlers
  (:require
   [y-video-back.db.files :as files]
   [y-video-back.db.content-files-assoc :as content_files_assoc]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service_handlers.utils :as utils]))

(def file-create
  {:summary "Creates a new file"
   :parameters {:body models/file_without_id}
   :responses {200 {:body {:message string?
                           :id string?}}}
   :handler (fn [{{:keys [body]} :parameters}]
              {:status 200
               :body {:message "1 file created"
                      :id (utils/get-id (files/CREATE body))}})})



(def file-get-by-id
  {:summary "Retrieves specified file"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body models/file}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [file_result (files/READ id)]
                (if (= "" (:id file_result))
                  {:status 404
                   :body {:message "requested file not found"}}
                  {:status 200
                   :body file_result})))})

(def file-update
  {:summary "Updates specified file"
   :parameters {:path {:id uuid?} :body ::sp/file}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path :keys [body]} :parameters}]
              (let [result (files/UPDATE id body)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested file not found"}}
                  {:status 200
                   :body {:message (str result " files updated")}})))})

(def file-delete
  {:summary "Deletes specified file"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [result (files/DELETE id)]
                (if (= 0 result)
                  {:status 404
                   :body {:message "requested file not found"}}
                  {:status 200
                   :body {:message (str result " files deleted")}})))})


(def file-get-all-contents ;; Non-functional
  {:summary "Retrieves all contents for specified file"
   :parameters {:path {:id uuid?}}
   :responses {200 {:body [(into models/content {:file-id uuid?})]}}
   :handler (fn [{{{:keys [id]} :path} :parameters}]
              (let [file_contents_result (content_files_assoc/READ-COLLECTIONS-BY-CONTENT id)]
                (let [content_result (map #(utils/remove-db-only %) file_contents_result)]
                  (if (= 0 (count content_result))
                    {:status 404
                     :body {:message "no files found for given content"}}
                    {:status 200
                     :body content_result}))))})