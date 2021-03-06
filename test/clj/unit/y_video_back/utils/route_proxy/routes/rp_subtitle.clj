(ns y-video-back.utils.route-proxy.routes.rp-subtitle
  (:require
    [y-video-back.config :refer [env]]
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]))


(defn subtitle-post
  "Create a subtitle via app's post request"
  ([session-id subtitle-without-id]
   (app (-> (request :post "/api/subtitle")
            (json-body subtitle-without-id)
            (header :session-id session-id))))
  ([subtitle-without-id]
   (subtitle-post (:session-id-bypass env) subtitle-without-id)))

(defn subtitle-id-get
  "Retrieves subtitle via app's get (id) request"
  ([session-id id]
   (app (-> (request :get (str "/api/subtitle/" id))
            (header :session-id session-id))))
  ([id]
   (subtitle-id-get (:session-id-bypass env) id)))

(defn subtitle-id-patch
  "Updates subtitle via app's patch (id) request"
  ([session-id id new-subtitle]
   (app (-> (request :patch (str "/api/subtitle/" id))
            (json-body new-subtitle)
            (header :session-id session-id))))
  ([id new-subtitle]
   (subtitle-id-patch (:session-id-bypass env) id new-subtitle)))

(defn subtitle-id-delete
  "Deletes subtitle via app's delete (id) request"
  ([session-id id]
   (app (-> (request :delete (str "/api/subtitle/" id))
            (header :session-id session-id))))
  ([id]
   (subtitle-id-delete (:session-id-bypass env) id)))
