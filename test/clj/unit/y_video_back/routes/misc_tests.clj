(ns y-video-back.routes.misc-tests
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
      [y-video-back.db.contents :as contents]
      [y-video-back.db.users-by-collection :as users-by-collection]
      [y-video-back.db.collections-courses-assoc :as collection-courses-assoc]
      [y-video-back.db.collections :as collections]
      [y-video-back.db.resources :as resources]
      [y-video-back.db.courses :as courses]
      [y-video-back.db.files :as files]
      [y-video-back.db.user-collections-assoc :as user-collections-assoc]
      [y-video-back.db.user-courses-assoc :as user-courses-assoc]
      [y-video-back.db.users :as users]
      [y-video-back.db.words :as words]
      [y-video-back.utils.utils :as ut]
      [y-video-back.routes.service-handlers.db-utils :as dbu]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (mount/start #'y-video-back.config/env
                 #'y-video-back.handler/app
                 #'y-video-back.db.core/*db*)
    (f)))

(tcore/basic-transaction-fixtures
  (def test-user-one (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-two (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-thr (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-user-fou (ut/under-to-hyphen (users/CREATE (g/get-random-user-without-id))))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-one)}))))
  (def test-coll-two (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-two)}))))
  (def test-coll-thr (ut/under-to-hyphen (collections/CREATE (into (g/get-random-collection-without-id-or-owner) {:owner (:id test-user-thr)}))))
  (def test-rsrc-one (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-rsrc-two (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-rsrc-thr (ut/under-to-hyphen (resources/CREATE (g/get-random-resource-without-id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-two (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-crse-thr (ut/under-to-hyphen (courses/CREATE (g/get-random-course-without-id))))
  (def test-file-one (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id (:id test-rsrc-one)))))
  (def test-file-two (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id (:id test-rsrc-two)))))
  (def test-file-thr (ut/under-to-hyphen (files/CREATE (g/get-random-file-without-id (:id test-rsrc-thr)))))
  (def test-word-one (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-one)))))
  (def test-word-two (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-two)))))
  (def test-word-thr (ut/under-to-hyphen (words/CREATE (g/get-random-word-without-id (:id test-user-thr)))))

  (def test-user-coll-one (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-one)
                                                                              :collection-id (:id test-coll-one)
                                                                              :account-role 0})))
  (def test-user-coll-two (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-two)
                                                                              :collection-id (:id test-coll-two)
                                                                              :account-role 0})))
  (def test-user-coll-thr (ut/under-to-hyphen (user-collections-assoc/CREATE {:user-id (:id test-user-thr)
                                                                              :collection-id (:id test-coll-thr)
                                                                              :account-role 0})))
  (def test-user-crse-one (ut/under-to-hyphen (user-courses-assoc/CREATE {:user-id (:id test-user-one)
                                                                          :course-id (:id test-crse-one)
                                                                          :account-role 0})))
  (def test-user-crse-two (ut/under-to-hyphen (user-courses-assoc/CREATE {:user-id (:id test-user-two)
                                                                          :course-id (:id test-crse-two)
                                                                          :account-role 0})))
  (def test-user-crse-thr (ut/under-to-hyphen (user-courses-assoc/CREATE {:user-id (:id test-user-thr)
                                                                          :course-id (:id test-crse-thr)
                                                                          :account-role 0})))
  (def test-user-crse-fou (ut/under-to-hyphen (user-courses-assoc/CREATE {:user-id (:id test-user-fou)
                                                                          :course-id (:id test-crse-one)
                                                                          :account-role 2})))
  (def test-cont-one (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-one) (:id test-rsrc-one)))))
  (def test-cont-two (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-two) (:id test-rsrc-two)))))
  (def test-cont-thr (ut/under-to-hyphen (contents/CREATE (g/get-random-content-without-id (:id test-coll-thr) (:id test-rsrc-thr)))))
  (def test-coll-crse-one (ut/under-to-hyphen (collection-courses-assoc/CREATE {:collection-id (:id test-coll-one)
                                                                                 :course-id (:id test-crse-one)})))
  (def test-coll-crse-two (ut/under-to-hyphen (collection-courses-assoc/CREATE {:collection-id (:id test-coll-two)
                                                                                 :course-id (:id test-crse-two)})))
  (def test-coll-crse-thr (ut/under-to-hyphen (collection-courses-assoc/CREATE {:collection-id (:id test-coll-thr)
                                                                                 :course-id (:id test-crse-thr)})))



  (mount.core/start #'y-video-back.handler/app))

(deftest test-cont-add-view
  (testing "content add view"
    (let [res (rp/content-id-add-view (:id test-cont-one))]
      (is (= 200 (:status res)))
      (let [new-content (contents/READ (:id test-cont-one))
            new-resource (resources/READ (:id test-rsrc-one))]
        (is (= (+ 1 (:views test-cont-one)) (:views new-content)))
        (is (= (+ 1 (:views test-rsrc-one)) (:views new-resource)))))))
