(ns y-video-back.utils.route-proxy.routes.rp-content
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]))

(defn content-id-add-view
  "Adds a view to content"
  ([session-id id]
   (app (-> (request :post (str "/api/content/" id "/add-view"))
            (header :session-id session-id))))
  ([id]
   (content-id-add-view (:session-id-bypass env) id)))

(defn content-post
  "Create a content via app's post request"
  ([session-id content-without-id]
   (app (-> (request :post "/api/content")
            (json-body content-without-id)
            (header :session-id session-id))))
  ([content-without-id]
   (content-post (:session-id-bypass env) content-without-id)))

(defn content-id-get
  "Retrieves content via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/content/" id))
            (header :session-id session-id))))
  ([id]
   (content-id-get (:session-id-bypass env) id)))

(defn content-id-patch
  "Updates content via app's patch (id) request"
  ([session-id id new-content]
   (app (-> (request :patch (str "/api/content/" id))
            (json-body new-content)
            (header :session-id session-id))))
  ([id new-content]
   (content-id-patch (:session-id-bypass env) id new-content)))

(defn content-id-delete
  "Deletes content via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/content/" id))
            (header :session-id session-id))))
  ([id]
   (content-id-delete (:session-id-bypass env) id)))

(defn content-id-add-subtitle
  "Connects subtitle and content"
  ([session-id content-id subtitle-id]
   (app (-> (request :post (str "/api/content/" content-id "/add-subtitle"))
            (header :session-id session-id)
            (json-body {:subtitle-id subtitle-id}))))
  ([content-id subtitle-id]
   (content-id-add-subtitle (:session-id-bypass env) content-id subtitle-id)))

(defn content-id-remove-subtitle
  "Connects subtitle and content"
  ([session-id content-id subtitle-id]
   (app (-> (request :post (str "/api/content/" content-id "/remove-subtitle"))
            (json-body {:subtitle-id subtitle-id})
            (header :session-id session-id))))
  ([content-id subtitle-id]
   (content-id-remove-subtitle (:session-id-bypass env) content-id subtitle-id)))
