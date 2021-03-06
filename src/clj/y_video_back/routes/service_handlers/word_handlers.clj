(ns y-video-back.routes.service-handlers.word-handlers
  (:require
   [y-video-back.db.words :as words]
   [y-video-back.db.users :as users]
   [y-video-back.models :as models]
   [y-video-back.model-specs :as sp]
   [y-video-back.routes.service-handlers.utils :as utils]
   [y-video-back.routes.service-handlers.role-utils :as ru]))

(def word-create
  {:summary "Creates a new word"
   :parameters {:header {:session-id uuid?}
                :body models/word-without-id}
   :responses {200 {:body {:message string?
                           :id string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [user-id]} :path :keys [body]} :parameters}]
              (if-not (users/EXISTS? (:user-id body))
                {:status 500
                 :body {:message "user not found"}
                 :headers {"session-id" session-id}}
                (if (words/EXISTS-BY-FIELDS? (:user-id body) (:word body) (:src-lang body) (:dest-lang body))
                  {:status 500
                   :body {:message "word already exists"}
                   :headers {"session-id" session-id}}
                  {:status 200
                   :body {:message "1 word created"
                          :id (utils/get-id (words/CREATE body))}
                   :headers {"session-id" session-id}})))})

(def word-get-by-id
  {:summary "Retrieves specified word"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/word}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (let [word-result (words/READ id)]
                (if (nil? word-result)
                  {:status 404
                   :body {:message "requested word not found"}
                   :headers {"session-id" session-id}}
                  {:status 200
                   :body word-result
                   :headers {"session-id" session-id}})))})

(def word-update
  {:summary "Updates specified word"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?} :body ::sp/word}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}
               500 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path :keys [body]} :parameters}]
              (if-not (words/EXISTS? id)
                {:status 404
                 :body {:message "word not found"}
                 :headers {"session-id" session-id}}
                (let [current-word (words/READ id)
                      proposed-word (merge current-word body)
                      same-name-word (first (words/READ-ALL-BY-FIELDS [(:user-id proposed-word)
                                                                       (:word proposed-word)
                                                                       (:src-lang proposed-word)
                                                                       (:dest-lang proposed-word)]))]
                  ; If there is a collision and the collision is not with self (i.e. word being changed)
                  (if (and (not (nil? same-name-word))
                           (not (= (:id current-word)
                                   (:id same-name-word))))
                    {:status 500
                     :body {:message "unable to update word, identical word likely exists"}
                     :headers {"session-id" session-id}}
                    (if-not (users/EXISTS? (:user-id proposed-word))
                      {:status 500
                       :body {:message "user not found"}
                       :headers {"session-id" session-id}}
                      (let [result (words/UPDATE id body)]
                        (if (= 0 result)
                          {:status 500
                           :body {:message "unable to update word"}
                           :headers {"session-id" session-id}}
                          {:status 200
                           :body {:message (str result " words updated")}
                           :headers {"session-id" session-id}})))))))})

(def word-delete
  {:summary "Deletes specified word"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body {:message string?}}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (let [result (words/DELETE id)]
                (if (nil? result)
                  {:status 404
                   :body {:message "requested word not found"}
                   :headers {"session-id" session-id}}
                  {:status 200
                   :body {:message (str result " words deleted")}
                   :headers {"session-id" session-id}})))})
