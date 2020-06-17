(ns y-video-back.routes.service-handlers.collection-handlers
  (:require
   [y-video-back.db.collections-contents-assoc :as collection-contents-assoc]
   [y-video-back.db.users-by-collection :as users-by-collection]
   [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
   [y-video-back.db.user-collections-assoc :as user-collections-assoc]
   [y-video-back.db.collections :as collections]
   [y-video-back.db.annotations :as annotations]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))


(def collection-create ;; Non-functional
  {:summary "Creates a new collection with the given (temp) user as an owner"
   :parameters {:header {:session-id uuid?}
                :body {:collection models/collection-without-id :user-id uuid?}}
   :responses {200 {:body {:message string?
                           :id string?}}
               409 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "collection-create" 0)
                ru/forbidden-page
                (try {:status 200
                      :body {:message "1 collection created"
                             :id (let [collection-id (utils/get-id (collections/CREATE (:collection body)))]
                                   (user-collections-assoc/CREATE {:user-id (:user-id body)
                                                                   :collection-id (utils/to-uuid collection-id)
                                                                   :account-role 0})
                                   collection-id)}}
                     (catch Exception e
                       {:status 409
                        :body {:message e}}))))})

(def collection-get-by-id ;; Not tested
  {:summary "Retrieves specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/collection}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "collection-get-by-id" {:collection-id id})
                ru/forbidden-page
                (let [res (collections/READ id)]
                  (if (nil? res)
                    {:status 404
                     :body {:message "requested collection not found"}}
                    {:status 200
                     :body res}))))})

(def collection-update ;; Non-functional
  {:summary "Updates the specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/collection}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "collection-update" {:collection-id id})
                ru/forbidden-page
                (let [result (collections/UPDATE id body)]
                  (if (nil? result)
                    {:status 404
                     :body {:message "requested collection not found"}}
                    {:status 200
                     :body {:message (str 1 " collections updated")}}))))})

(def collection-delete ;; Non-functional
  {:summary "Deletes the specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "collection-delete" 0)
                ru/forbidden-page
                (let [result (collections/DELETE id)]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "requested collection not found"}}
                    {:status 200
                     :body {:message (str 1 " collections deleted")}}))))})

(def collection-add-user
  {:summary "Adds user to specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:user-id uuid? :account-role int?}}
   :responses {200 {:body {:message string? :id string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "collection-add-user" {:collection-id id})
                ru/forbidden-page
                (let [result (utils/get-id (user-collections-assoc/CREATE (into body {:collection-id id})))]
                  (if (= nil result)
                    {:status 404
                     :body {:message "unable to add user"}}
                    {:status 200
                     :body {:message (str 1 " users added to collection")
                            :id result}}))))})

(def collection-remove-user
  {:summary "Removes user from specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:user-id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "collection-remove-user" {:collection-id id})
                ru/forbidden-page
                (let [result (user-collections-assoc/DELETE-BY-IDS [id (:user-id body)])]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "unable to remove user"}}
                    {:status 200
                     :body {:message (str result " users removed from collection")}}))))})

(def collection-add-content
  {:summary "Adds content to specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:content-id uuid?}}
   :responses {200 {:body {:message string? :id string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "collection-add-content" {:collection-id id})
                ru/forbidden-page
                (let [result (utils/get-id (annotations/CREATE (into body {:collection-id id})))]
                  (if (= nil result)
                    {:status 404
                     :body {:message "unable to add content"}}
                    {:status 200
                     :body {:message (str 1 " contents added to collection")
                            :id result}}))))})

(def collection-remove-content
  {:summary "Removes content from specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:content-id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "collection-remove-content" {:collection-id id})
                ru/forbidden-page
                (let [result (annotations/DELETE-BY-IDS [id (:content-id body)])]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "unable to remove content"}}
                    {:status 200
                     :body {:message (str result " contents removed from collection")}}))))})

(def collection-add-course
  {:summary "Adds course to specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:course-id uuid?}}
   :responses {200 {:body {:message string? :id string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "collection-add-course" {:collection-id id})
                ru/forbidden-page
                (let [result (utils/get-id (collection-courses-assoc/CREATE (into body {:collection-id id})))]
                  (if (= nil result)
                    {:status 404
                     :body {:message "unable to add course"}}
                    {:status 200
                     :body {:message (str 1 " courses added to collection")
                            :id result}}))))})

(def collection-remove-course
  {:summary "Removes course from specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body {:course-id uuid?}}
   :responses {200 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (ru/has-permission session-id "collection-remove-course" {:collection-id id})
                ru/forbidden-page
                (let [result (collection-courses-assoc/DELETE-BY-IDS [id (:course-id body)])]
                  (if (= 0 result)
                    {:status 404
                     :body {:message "unable to remove course"}}
                    {:status 200
                     :body {:message (str result " courses removed from collection")}}))))})


(def collection-get-all-contents ;; Non-functional
  {:summary "Retrieves all the contents for the specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/content {:collection-id uuid?})]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "collection-get-all-contents" {:collection-id id})
                ru/forbidden-page
                (let [content-collections-result (annotations/READ-CONTENTS-BY-COLLECTION id)]
                  (let [content-result (map #(utils/remove-db-only %) content-collections-result)]
                    (if (= 0 (count content-result))
                      {:status 404
                       :body {:message "no contents found for given collection"}}
                      {:status 200
                       :body content-result})))))})

(def collection-get-all-courses ;; Non-functional
  {:summary "Retrieves all the courses for the specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/course {:collection-id uuid?})]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "collection-get-all-courses" {:collection-id id})
                ru/forbidden-page
                (let [course-collections-result (collection-courses-assoc/READ-COURSES-BY-COLLECTION id)]
                  (let [course-result (map #(utils/remove-db-only %) course-collections-result)]
                    (if (= 0 (count course-result))
                      {:status 404
                       :body {:message "no courses found for given collection"}}
                      {:status 200
                       :body course-result})))))})

(def collection-get-all-users
  {:summary "Retrieves all users for the specified collection"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body [(into models/user {:account-role int? :collection-id uuid?})]}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "collection-get-all-users" {:collection-id id})
                ru/forbidden-page
                (let [user-collections-result (user-collections-assoc/READ-USERS-BY-COLLECTION id)]
                  (let [user-result (map #(utils/remove-db-only %) user-collections-result)]
                    (if (= 0 (count user-result))
                      {:status 404
                       :body {:message "no users found for given collection"}}
                      {:status 200
                       :body user-result})))))})
