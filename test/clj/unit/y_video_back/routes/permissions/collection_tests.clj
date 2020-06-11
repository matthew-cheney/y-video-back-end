(ns y-video-back.routes.permissions.collection-tests
  (:require
    [clojure.test :refer :all]
    [ring.mock.request :refer :all]
    [y-video-back.handler :refer :all]
    [y-video-back.db.test-util :as tcore]
    [muuntaja.core :as m]
    [clojure.java.jdbc :as jdbc]
    [mount.core :as mount]
    [y-video-back.utils.model_generator :as g]
    [y-video-back.utils.route_proxy :as rp]
    [y-video-back.db.core :refer [*db*] :as db]
    [y-video-back.db.annotations :as annotations]
    [y-video-back.db.collections-contents-assoc :as collection_contents_assoc]
    [y-video-back.db.users-by-collection :as users-by-collection]
    [y-video-back.db.collections-courses-assoc :as collection_courses_assoc]
    [y-video-back.db.collections :as collections]
    [y-video-back.db.content-files-assoc :as content_files_assoc]
    [y-video-back.db.contents :as contents]
    [y-video-back.db.courses :as courses]
    [y-video-back.db.files :as files]
    [y-video-back.db.user-collections-assoc :as user_collections_assoc]
    [y-video-back.db.user-courses-assoc :as user_courses_assoc]
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
  (def user-admin-one (users/CREATE {:email "admin_1@gmail.com"
                                     :last-login "never"
                                     :account-name "Mr. Admin 1"
                                     :account-type 0
                                     :username "a1"}))
  (def user-la-one (users/CREATE {:email "la_1@gmail.com"
                                  :last-login "never"
                                  :account-name "Mr. Assistant 1"
                                  :account-type 1
                                  :username "l1"}))
  (def user-instr-one (users/CREATE {:email "instructor_1@gmail.com"
                                     :last-login "never"
                                     :account-name "Mr. Instructor 1"
                                     :account-type 2
                                     :username "i1"}))
  (def user-instr-two (users/CREATE {:email "instructor_2@gmail.com"
                                     :last-login "never"
                                     :account-name "Mr. Instructor 2"
                                     :account-type 2
                                     :username "i2"}))
  (def user-instr-thr (users/CREATE {:email "instructor_3@gmail.com"
                                     :last-login "never"
                                     :account-name "Mr. Instructor 3"
                                     :account-type 2
                                     :username "i3"}))
  (def user-student-one (users/CREATE {:email "student_1@gmail.com"
                                       :last-login "never"
                                       :account-name "Mr. Student 1"
                                       :account-type 3
                                       :username "s1"}))
  (def user-student-two (users/CREATE {:email "student_2@gmail.com"
                                       :last-login "never"
                                       :account-name "Mr. Student 2"
                                       :account-type 3
                                       :username "s2"}))
  (def user-student-thr (users/CREATE {:email "student_3@gmail.com"
                                       :last-login "never"
                                       :account-name "Mr. Student 3"
                                       :account-type 3
                                       :username "s3"}))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE (g/get_random_collection_without_id))))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE (g/get_random_course_without_id))))
  (def test-user-crse-one (ut/under-to-hyphen (user_courses_assoc/CREATE {:user_id (:id user-student-one)
                                                                          :course_id (:id test-crse-one)
                                                                          :account_role 2}))) ; student in course
  (def test-user-crse-two (ut/under-to-hyphen (user_courses_assoc/CREATE {:user_id (:id user-instr-one)
                                                                          :course_id (:id test-crse-one)
                                                                          :account_role 0})))
  (def test-user-coll-one (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id user-instr-one)
                                                                              :collection_id (:id test-coll-one)
                                                                              :account_role 0}))) ; owner of collection
  (def test-user-coll-two (ut/under-to-hyphen (user_collections_assoc/CREATE {:user_id (:id user-student-two)
                                                                              :collection_id (:id test-coll-one)
                                                                              :account_role 1}))) ; TA for collection
  (def test-coll-crse-one (ut/under-to-hyphen (collection_courses_assoc/CREATE {:collection_id (:id test-coll-one)
                                                                                :course_id (:id test-crse-one)})))

  (mount.core/start #'y-video-back.handler/app))


(deftest dummy
  (is (= 0 0)))

; (no|with) connection -> (not) connected via many-to-many table

; Create collection
(deftest collection-create
  (testing "student create collection, no connection"
    (let [res (rp/collection-post (:id user-student-one)
                                  (g/get_random_collection_without_id)
                                  (:id user-student-one))]
      (is (= 401 (:status res)))))
  (testing "instr create collection"
    (let [res (rp/collection-post (:id user-instr-one)
                                  (g/get_random_collection_without_id)
                                  (:id user-instr-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant create collection"
    (let [res (rp/collection-post (:id user-la-one)
                                  (g/get_random_collection_without_id)
                                  (:id user-instr-one))]
      (is (= 200 (:status res)))))
  (testing "admin create collection"
    (let [res (rp/collection-post (:id user-admin-one)
                                  (g/get_random_collection_without_id)
                                  (:id user-instr-one))]
      (is (= 200 (:status res))))))

; Retrieve collection
(deftest collection-read
  (testing "student reading collection, no connection"
    (let [res (rp/collection-id-get (:id user-student-thr)
                                    (:id test-coll-one))]
      (is (= 401 (:status res)))))
  (testing "instructor reading collection, no connection"
    (let [res (rp/collection-id-get (:id user-instr-two)
                                    (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "lab assistant reading collection, no connection"
    (let [res (rp/collection-id-get (:id user-la-one)
                                    (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "admin reading collection, no connection"
    (let [res (rp/collection-id-get (:id user-admin-one)
                                    (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "student reading collection, with connection (student)"
    (let [res (rp/collection-id-get (:id user-student-one)
                                    (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "student reading collection, with connection (TA)"
    (let [res (rp/collection-id-get (:id user-student-two)
                                    (:id test-coll-one))]
      (is (= 200 (:status res)))))
  (testing "instructor reading collection, with connection"
    (let [res (rp/collection-id-get (:id user-instr-two)
                                    (:id test-coll-one))]
      (is (= 200 (:status res))))))

; Update collection
; Delete collection
; Connect user and collection
; Disconnects user and collection
; Connect content and collection
; Disconnects content and collection
; Retrieve all contents for collection
; Retrieve all courses for collection
; Retrieve all users for collection
; Connect course and collection
; Disconnects course and collection



(deftest no-session-id-for-testing
  (testing "no session id"
    (let [res (rp/echo-post "6bc824a6-f446-416d-8dd6-06350ae577f4" "hey")]
      (is (= 200 (:status res)))
      (is (= {:echo "hey"} (m/decode-response-body res))))
    (let [res (rp/echo-post "there")]
      (is (= 200 (:status res)))
      (is (= {:echo "there"} (m/decode-response-body res))))))

(deftest test-collection-add-user
  (testing "student adding student, no connection to coll"
    (let [res (rp/collection-id-add-user (:id user-student-one) ; session-id
                                         (:id test-coll-one)
                                         (:id user-student-two)
                                         0)]
      (is (= 401 (:status res)))))
  (testing "instructor adding student"
    (let [res (rp/collection-id-add-user (:id user-instr-one) ; session-id
                                         (:id test-coll-one)
                                         (:id user-student-one)
                                         0)]
      (is (= 200 (:status res))))))
