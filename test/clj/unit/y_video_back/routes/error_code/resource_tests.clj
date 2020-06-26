(ns y-video-back.routes.error-code.resource-tests
    (:require
      [clojure.test :refer :all]
      [ring.mock.request :refer :all]
      [y-video-back.handler :refer :all]
      [y-video-back.db.test-util :as tcore]
      [muuntaja.core :as m]
      [clojure.java.jdbc :as jdbc]
      [mount.core :as mount]
      [y-video-back.utils.model-generator :as g]
      [y-video-back.utils.route-proxy :as rp]
      [y-video-back.db.core :refer [*db*] :as db]
      [y-video-back.db.contents :as contents]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resource-files-assoc :as resource-files-assoc]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.utils.utils :as ut]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  ;(def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  ;(def test-user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  ;(def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  ;(def test-cont-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  ;(def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-cont-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id))))
  (mount.core/start #'y-video-back.handler/app))

; The only error thrown for resource-post is request body coercion (400)

(deftest resource-id-get
  (testing "read nonexistent resource"
    (let [res (rp/resource-id-get (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest resource-id-patch
  (testing "update nonexistent resource"
    (let [res (rp/resource-id-patch (java.util.UUID/randomUUID) (g/get-random-resource-without-id))]
      (is (= 404 (:status res))))))

(deftest resource-id-delete
  (testing "delete nonexistent resource"
    (let [res (rp/resource-id-delete (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest resource-get-all-collections
  (testing "get collections for nonexistent resource"
    (let [res (rp/resource-id-collections (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get collections for resource with no collections"
    (let [new-resource (g/get-random-resource-without-id)
          add-cont-res (resources/CREATE new-resource)
          res (rp/resource-id-collections (:id add-cont-res))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))

(deftest resource-get-all-files
  (testing "get files for nonexistent resource"
    (let [res (rp/resource-id-files (java.util.UUID/randomUUID))]
      (is (= 404 (:status res)))))
  (testing "get files for resource with no files"
    (let [new-resource (g/get-random-resource-without-id)
          add-cont-res (resources/CREATE new-resource)
          res (rp/resource-id-files (:id add-cont-res))]
      (is (= 200 (:status res)))
      (is (= '() (m/decode-response-body res))))))

(deftest resource-add-view
  (testing "add view to nonexistent resource"
    (let [res (rp/resource-id-add-view (java.util.UUID/randomUUID))]
      (is (= 404 (:status res))))))

(deftest resource-add-file
  (testing "add nonexistent file to resource"
    (let [res (rp/resource-id-add-file (:id test-cont-one) (java.util.UUID/randomUUID))]
      (is (= 500 (:status res)))))
  (testing "add file to nonexistent resource"
    (let [res (rp/resource-id-add-file (java.util.UUID/randomUUID) (:id test-file-one))]
      (is (= 404 (:status res)))))
  (testing "add file to resource, already connected"
    (let [new-file (files/CREATE (g/get-random-file-without-id))
          connect-file-res (resource-files-assoc/CREATE {:file-id (:id new-file)
                                                        :resource-id (:id test-cont-one)})
          res (rp/resource-id-add-file (:id test-cont-one) (:id new-file))]
      (is (= 500 (:status res))))))

(deftest resource-remove-file
  (testing "remove nonexistent file from resource"
    (let [res (rp/resource-id-remove-file (:id test-cont-one) (java.util.UUID/randomUUID))]
      (is (= 500 (:status res)))))
  (testing "remove file from nonexistent resource"
    (let [res (rp/resource-id-remove-file (java.util.UUID/randomUUID) (:id test-file-one))]
      (is (= 404 (:status res)))))
  (testing "remove file from resource, not connected"
    (let [new-file (files/CREATE (g/get-random-file-without-id))
          res (rp/resource-id-remove-file (:id test-cont-one) (:id new-file))]
      (is (= 500 (:status res))))))
