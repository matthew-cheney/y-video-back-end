(ns y-video-back.routes.language
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model-generator :as g]
      [y-video-back.utils.route-proxy.proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.utils.utils :as ut]
      [y-video-back.utils.db-populator :as db-pop]
      [y-video-back.db.languages :as languages]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  (mount.core/start #'y-video-back.handler/app))

(deftest test-lang
  (testing "lang CREATE"
    (let [lang-one (db-pop/get-language)]
      (let [res (rp/language-post lang-one)]
        (is (= 200 (:status res)))
        (let [id (ut/to-uuid (:id (m/decode-response-body res)))]
          (is (= (into lang-one {:id id})
                 (ut/remove-db-only (languages/READ id))))))))
  (testing "lang READ"
    (let [lang-one (ut/under-to-hyphen (languages/CREATE (db-pop/get-language)))
          res (rp/language-id-get (:id lang-one))]
      (is (= 200 (:status res)))
      (is (= (-> lang-one
                 (ut/remove-db-only)
                 (update :id str))
             (ut/remove-db-only (m/decode-response-body res))))))
  (testing "language UPDATE"
    (let [lang-one (languages/CREATE (db-pop/get-language))
          lang-two (db-pop/get-language)]
      (let [res (rp/language-id-patch (:id lang-one) lang-two)]
        (is (= 200 (:status res)))
        (is (= (into lang-two {:id (:id lang-one)}) (ut/remove-db-only (languages/READ (:id lang-one))))))))
  (testing "language DELETE"
    (let [lang-one (languages/CREATE (db-pop/get-language))
          res (rp/language-id-delete (:id lang-one))]
      (is (= 200 (:status res)))
      (is (= nil (languages/READ (:id lang-one)))))))

(deftest lang-get-all-0
  "get all languages from db (0)"
  (let [res (rp/language-get-all)]
    (is (= 200 (:status res)))
    (is (= []
           (m/decode-response-body res)))))

(deftest lang-get-all-1
  "get all languages from db (1)"
  (let [lang-one (db-pop/add-language)
        res (rp/language-get-all)]
    (is (= 200 (:status res)))
    (is (= (frequencies (map #(update % :id str) [lang-one]))
           (frequencies (m/decode-response-body res))))))

(deftest lang-get-all-3
  "get all languages from db (3)"
  (let [lang-one (db-pop/add-language)
        lang-two (db-pop/add-language)
        lang-thr (db-pop/add-language)
        res (rp/language-get-all)]
    (is (= 200 (:status res)))
    (is (= (frequencies (map #(update % :id str) [lang-one lang-two lang-thr]))
           (frequencies (m/decode-response-body res))))))