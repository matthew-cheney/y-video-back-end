(ns y-video-back.routes.permissions.account-role.collection-tests
  (:require
    [y-video-back.config :refer [env]]
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
    [y-video-back.db.collections-contents-assoc :as collection-contents-assoc]
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
    [y-video-back.utils.db-populator :as db-pop]
    [y-video-back.user-creator :as uc]))

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

;post: /api/collection
(deftest collection-post
  (testing "instructor, collection-post, self as owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/get-collection (:id user-one))
          res (rp/collection-post (uc/user-id-to-session-id (:id user-one))
                                  coll-one)]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-post, other instructor as owner"
    (let [user-one (db-pop/add-user "instructor")
          user-two (db-pop/add-user "instructor")
          coll-one (db-pop/get-collection (:id user-two))
          res (rp/collection-post (uc/user-id-to-session-id (:id user-one))
                                  coll-one)]
      (is (= 401 (:status res)))))
  (testing "student, collection-post, self as owner"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/get-collection (:id user-one))
          res (rp/collection-post (uc/user-id-to-session-id (:id user-one))
                                  coll-one)]
      (is (= 401 (:status res))))))

;get: /api/collection/{id}
(deftest collection-get-by-id
  (testing "instructor, collection-get-by-id, owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-get-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "instructor")
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-get-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-get-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "ta")
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-get-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-get-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "student")
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-get-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-get-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "auditing")
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-get-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-get (uc/user-id-to-session-id (:id user-one))
                                    (:id coll-one))]
      (is (= 200 (:status res))))))

;delete: /api/collection/{id}
(deftest collection-delete-by-id
  (testing "instructor, collection-delete-by-id, owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "instructor, collection-delete-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "instructor")
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "instructor, collection-delete-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-delete-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "ta")
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-delete-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-delete-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "student")
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-delete-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-delete-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "auditing")
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-delete-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-delete (uc/user-id-to-session-id (:id user-one))
                                       (:id coll-one))]
      (is (= 401 (:status res))))))

;patch: /api/collection/{id}
(deftest collection-patch-by-id
  (testing "instructor, collection-patch-by-id, owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      coll-one)]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-patch-by-id, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "instructor")
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      coll-one)]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-patch-by-id, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      coll-one)]
      (is (= 200 (:status res)))))
  (testing "student, collection-patch-by-id, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "ta")
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      coll-one)]
      (is (= 200 (:status res)))))
  (testing "student, collection-patch-by-id, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      coll-one)]
      (is (= 200 (:status res)))))
  (testing "student, collection-patch-by-id, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "student")
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      coll-one)]
      (is (= 401 (:status res)))))
  (testing "student, collection-patch-by-id, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      coll-one)]
      (is (= 401 (:status res)))))
  (testing "student, collection-patch-by-id, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "auditing")
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      coll-one)]
      (is (= 401 (:status res)))))
  (testing "student, collection-patch-by-id, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-patch (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one)
                                      coll-one)]
      (is (= 401 (:status res))))))

;post: /api/collection/{id}/add-user
(deftest collection-add-user
  (testing "instructor, collection-add-user, owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          user-two (db-pop/add-user)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)] ; role doesn't matter here
      (is (= 200 (:status res)))))
  (testing "instructor, collection-add-user, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "instructor")
          user-two (db-pop/add-user)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)] ; role doesn't matter here
      (is (= 200 (:status res)))))
  (testing "instructor, collection-add-user, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          user-two (db-pop/add-user)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)] ; role doesn't matter here
      (is (= 200 (:status res)))))
  (testing "student, collection-add-user, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "ta")
          user-two (db-pop/add-user)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)] ; role doesn't matter here
      (is (= 401 (:status res)))))
  (testing "student, collection-add-user, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          user-two (db-pop/add-user)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)] ; role doesn't matter here
      (is (= 401 (:status res)))))
  (testing "student, collection-add-user, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "student")
          user-two (db-pop/add-user)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)] ; role doesn't matter here
      (is (= 401 (:status res)))))
  (testing "student, collection-add-user, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          user-two (db-pop/add-user)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)] ; role doesn't matter here
      (is (= 401 (:status res)))))
  (testing "student, collection-add-user, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "auditing")
          user-two (db-pop/add-user)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)] ; role doesn't matter here
      (is (= 401 (:status res)))))
  (testing "student, collection-add-user, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          user-two (db-pop/add-user)
          res (rp/collection-id-add-user (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one)
                                         (:username user-two)
                                         0)] ; role doesn't matter here
      (is (= 401 (:status res))))))

;post: /api/collection/{id}/remove-user
(deftest collection-remove-user
  (testing "instructor, collection-remove-user, owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                            (:id coll-one)
                                            (:username user-two))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-remove-user, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "instructor")
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                            (:id coll-one)
                                            (:username user-two))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-remove-user, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                            (:id coll-one)
                                            (:username user-two))]
      (is (= 200 (:status res)))))
  (testing "student, collection-remove-user, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "ta")
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                            (:id coll-one)
                                            (:username user-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-user, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                            (:id coll-one)
                                            (:username user-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-user, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "student")
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                            (:id coll-one)
                                            (:username user-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-user, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                            (:id coll-one)
                                            (:username user-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-user, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "auditing")
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                            (:id coll-one)
                                            (:username user-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-user, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          user-two (db-pop/add-user)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-two) (:id coll-one))
          res (rp/collection-id-remove-user (uc/user-id-to-session-id (:id user-one))
                                            (:id coll-one)
                                            (:username user-two))]
      (is (= 401 (:status res))))))

;post: /api/collection/{id}/add-course
(deftest collection-remove-course
  (testing "instructor, collection-remove-course, owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          crse-two (db-pop/add-course)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:id crse-two))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-remove-course, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "instructor")
          crse-two (db-pop/add-course)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:id crse-two))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-remove-course, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          crse-two (db-pop/add-course)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:id crse-two))]
      (is (= 200 (:status res)))))
  (testing "student, collection-remove-course, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "ta")
          crse-two (db-pop/add-course)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:id crse-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-course, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          crse-two (db-pop/add-course)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:id crse-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-course, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "student")
          crse-two (db-pop/add-course)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:id crse-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-course, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          crse-two (db-pop/add-course)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:id crse-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-course, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "auditing")
          crse-two (db-pop/add-course)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:id crse-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-course, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          crse-two (db-pop/add-course)
          res (rp/collection-id-add-course (uc/user-id-to-session-id (:id user-one))
                                           (:id coll-one)
                                           (:id crse-two))]
      (is (= 401 (:status res))))))

;post: /api/collection/{id}/remove-course
(deftest collection-remove-course
  (testing "instructor, collection-remove-course, owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          crse-two (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-two))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-two))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-remove-course, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "instructor")
          crse-two (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-two))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-two))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-remove-course, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          crse-two (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-two))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-two))]
      (is (= 200 (:status res)))))
  (testing "student, collection-remove-course, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "ta")
          crse-two (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-two))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-course, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          crse-two (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-two))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-course, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "student")
          crse-two (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-two))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-course, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          crse-two (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-two))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-course, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "auditing")
          crse-two (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-two))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-two))]
      (is (= 401 (:status res)))))
  (testing "student, collection-remove-course, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          crse-two (db-pop/add-course)
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-two))
          res (rp/collection-id-remove-course (uc/user-id-to-session-id (:id user-one))
                                              (:id coll-one)
                                              (:id crse-two))]
      (is (= 401 (:status res))))))

;get: /api/collection/{id}/contents
(deftest collection-id-contents
  (testing "instructor, collection-id-contents, owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-id-contents, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "instructor")
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-id-contents, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-id-contents, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "ta")
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-id-contents, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-id-contents, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "student")
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-id-contents, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-id-contents, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "auditing")
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-id-contents, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-contents (uc/user-id-to-session-id (:id user-one))
                                         (:id coll-one))]
      (is (= 200 (:status res))))))

;get: /api/collection/{id}/courses
(deftest collection-id-courses
  (testing "instructor, collection-id-courses, owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-id-courses, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "instructor")
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-id-courses, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-id-courses, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "ta")
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-id-courses, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-id-courses, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "student")
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-id-courses, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-id-courses, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "auditing")
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-id-courses, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-courses (uc/user-id-to-session-id (:id user-one))
                                        (:id coll-one))]
      (is (= 401 (:status res))))))

;get: /api/collection/{id}/users
(deftest collection-id-users
  (testing "instructor, collection-id-users, owner"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection (:id user-one))
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-id-users, instructor via user-coll"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "instructor")
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor, collection-id-users, instructor via course"
    (let [user-one (db-pop/add-user "instructor")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "instructor")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 200 (:status res)))))
  (testing "student, collection-id-users, ta via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "ta")
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-id-users, ta via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "ta")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-id-users, student via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "student")
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-id-users, student via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "student")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-id-users, auditing via user-coll"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          user-coll-add (db-pop/add-user-coll-assoc (:id user-one) (:id coll-one) "auditing")
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 401 (:status res)))))
  (testing "student, collection-id-users, auditing via course"
    (let [user-one (db-pop/add-user "student")
          coll-one (db-pop/add-collection)
          crse-one (db-pop/add-course)
          user-crse-add (db-pop/add-user-crse-assoc (:id user-one) (:id crse-one) "auditing")
          coll-crse-add (db-pop/add-coll-crse-assoc (:id coll-one) (:id crse-one))
          res (rp/collection-id-users (uc/user-id-to-session-id (:id user-one))
                                      (:id coll-one))]
      (is (= 401 (:status res))))))