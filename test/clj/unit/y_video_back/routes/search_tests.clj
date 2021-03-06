(ns y-video-back.routes.search-tests
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
  (def test-user-one (ut/under-to-hyphen (users/CREATE {:email "im.a.what@gmail.com"
                                                        :last-login "11 years ago"
                                                        :account-name "Harry Potter"
                                                        :account-type 0
                                                        :username "1-flyboy"})))
  (def test-user-two (ut/under-to-hyphen (users/CREATE {:email "another-weasley@gmail.com"
                                                        :last-login "yesterday"
                                                        :account-name "Ron Weasley"
                                                        :account-type 1
                                                        :username "King-1-Weasley"})))
  (def test-user-thr (ut/under-to-hyphen (users/CREATE {:email "insufferable-know-it-all@byu.edu"
                                                        :last-login "today"
                                                        :account-name "Hermione Granger"
                                                        :account-type 0
                                                        :username "future-minister-1"})))
  (def test-coll-one (ut/under-to-hyphen (collections/CREATE {:collection-name "Quidditch Books"
                                                              :published true
                                                              :archived false
                                                              :owner (:id test-user-one)})))
  (def test-coll-two (ut/under-to-hyphen (collections/CREATE {:collection-name "Self-help Books"
                                                              :published true
                                                              :archived true
                                                              :owner (:id test-user-two)})))
  (def test-coll-thr (ut/under-to-hyphen (collections/CREATE {:collection-name "Non-fiction Books"
                                                              :published false
                                                              :archived true
                                                              :owner (:id test-user-thr)})))
  (def test-rsrc-one (ut/under-to-hyphen (resources/CREATE {:resource-name "Quidditch Through the Ages"
                                                            :resource-type "book 1"
                                                            :requester-email "im.a.what@gmail.com"
                                                            :copyrighted true
                                                            :physical-copy-exists false
                                                            :full-video true
                                                            :published false
                                                            :date-validated "a while ago"
                                                            :views 0
                                                            :all_file_versions "[book]"
                                                            :metadata "so meta"})))
  (def test-rsrc-two (ut/under-to-hyphen (resources/CREATE {:resource-name "Twelve Fail-Safe Ways to Charm Witches"
                                                            :resource-type "book 2"
                                                            :requester-email "another-weasley@gmail.com"
                                                            :copyrighted false
                                                            :physical-copy-exists true
                                                            :full-video false
                                                            :published true
                                                            :date-validated "recently"
                                                            :views 0
                                                            :all_file_versions "[book]"
                                                            :metadata "so meta"})))
  (def test-rsrc-thr (ut/under-to-hyphen (resources/CREATE {:resource-name "Hogwarts: A History"
                                                            :resource-type "book 2"
                                                            :requester-email "insufferable-know-it-all@byu.edu"
                                                            :copyrighted false
                                                            :physical-copy-exists false
                                                            :full-video false
                                                            :published false
                                                            :date-validated "every day"
                                                            :views 0
                                                            :all_file_versions "[book]"
                                                            :metadata "so meta"})))
  (def test-crse-one (ut/under-to-hyphen (courses/CREATE {:department "Transfiguration"
                                                          :catalog-number "ClockAndMap 101"
                                                          :section-number "001"})))
  (def test-crse-two (ut/under-to-hyphen (courses/CREATE {:department "Potions"
                                                          :catalog-number "UWillFail 101"
                                                          :section-number "001"})))
  (def test-crse-thr (ut/under-to-hyphen (courses/CREATE {:department "Charms"
                                                          :catalog-number "CouldveBeenDualing 101"
                                                          :section-number "001"})))
  (def test-cont-one (ut/under-to-hyphen (contents/CREATE {:title "aaaa"
                                                           :content-type "b"
                                                           :url "cccc"
                                                           :description "dddd"
                                                           :tags "e; e; ee; eee"
                                                           :annotations ""
                                                           :thumbnail ""
                                                           :allow-definitions false
                                                           :allow-notes false
                                                           :allow-captions false
                                                           :views 0
                                                           :file-version "f"
                                                           :resource-id (:id test-rsrc-one)
                                                           :collection-id (:id test-coll-one)})))
  (def test-cont-two (ut/under-to-hyphen (contents/CREATE {:title "aaa"
                                                           :content-type "g"
                                                           :url "adfwe"
                                                           :description "awgwab"
                                                           :tags "e; e; rbe; eee"
                                                           :annotations ""
                                                           :thumbnail ""
                                                           :allow-definitions false
                                                           :allow-notes false
                                                           :allow-captions false
                                                           :views 0
                                                           :file-version "f"
                                                           :resource-id (:id test-rsrc-two)
                                                           :collection-id (:id test-coll-two)})))
  (def test-cont-thr (ut/under-to-hyphen (contents/CREATE {:title "aa"
                                                           :content-type "h"
                                                           :url "cebreabccc"
                                                           :description "dtrntnrstddd"
                                                           :tags "e; ertnsre; ee; eee"
                                                           :annotations ""
                                                           :thumbnail ""
                                                           :allow-definitions false
                                                           :allow-notes false
                                                           :allow-captions false
                                                           :views 0
                                                           :file-version "f"
                                                           :resource-id (:id test-rsrc-thr)
                                                           :collection-id (:id test-coll-thr)})))



  (mount.core/start #'y-video-back.handler/app))

; For testing master search route - may add that feature in future
;(defn test-search-table
;  [table-key query-term expected-users]
;  (let [res (rp/search query-term)]
;    (is (= 200 (:status res)))
;    (if (= table-key :collections)
;      (is (= (into [] (map #(update (update (ut/remove-db-only %) :id str) :owner str) expected-users))
;             (table-key (m/decode-response-body res))
;      (is (= (into [] (map #(update (ut/remove-db-only %) :id str) expected-users))
;             (table-key (m/decode-response-body res))))

(defn test-search-table
  [table-key query-term expected-res]
  (let [res (case table-key
              :users (rp/search-by-user query-term)
              :collections (rp/search-by-collection query-term)
              :resources (rp/search-by-resource query-term)
              :contents (rp/search-by-content query-term))]
    (is (= 200 (:status res)))
    (if (= table-key :collections)
      (is (= (into [] (map #(update (update (ut/remove-db-only %) :id str) :owner str) expected-res))
             (m/decode-response-body res)))
      (is (= (into [] (map #(update (ut/remove-db-only %) :id str) expected-res))
             (m/decode-response-body res))))))

(deftest test-search-users
  (testing "all users email"
    (test-search-table :users
                       "@"
                       [test-user-one test-user-two test-user-thr]))
  (testing "one user email"
    (test-search-table :users
                       "im.a.what"
                       [test-user-one]))
  (testing "no users email"
    (test-search-table :users
                       "@yahoo.com"
                       []))
  (testing "all users name"
    (test-search-table :users
                       "o"
                       [test-user-one test-user-two test-user-thr]))
  (testing "one user name"
    (test-search-table :users
                       "Ron W"
                       [test-user-two]))
  (testing "no users name"
    (test-search-table :users
                       "Malfoy"
                       []))
  (testing "all users username"
    (test-search-table :users
                       "1"
                       [test-user-one test-user-two test-user-thr]))
  (testing "one user username"
    (test-search-table :users
                       "minister"
                       [test-user-thr]))
  (testing "no users username"
    (test-search-table :users
                       "buckbeak"
                       []))
  (testing "only a space"
    (test-search-table :users
                       " "
                       [test-user-one test-user-two test-user-thr])))
(deftest test-search-collections
  (testing "all colls name"
    (test-search-table :collections
                       "Books"
                       [(assoc test-coll-one :username (:username test-user-one))
                        (assoc test-coll-two :username (:username test-user-two))
                        (assoc test-coll-thr :username (:username test-user-thr))]))
  (testing "one coll name"
    (test-search-table :collections
                       "fiction"
                       [(assoc test-coll-thr :username (:username test-user-thr))]))
  (testing "no colls name"
    (test-search-table :collections
                       "Movies"
                       []))
  (testing "only a space"
    (test-search-table :collections
                       " "
                       [(assoc test-coll-one :username (:username test-user-one))
                        (assoc test-coll-two :username (:username test-user-two))
                        (assoc test-coll-thr :username (:username test-user-thr))])))
(deftest test-search-resources
  (testing "all conts name"
    (test-search-table :resources
                       "i"
                       [test-rsrc-one test-rsrc-two test-rsrc-thr]))
  (testing "one cont name"
    (test-search-table :resources
                       "elve"
                       [test-rsrc-two]))
  (testing "no conts name"
    (test-search-table :resources
                       "Fantastic Beasts"
                       []))
  (testing "all conts type"
    (test-search-table :resources
                       "book"
                       [test-rsrc-one test-rsrc-two test-rsrc-thr]))
  (testing "one cont type"
    (test-search-table :resources
                       "1"
                       [test-rsrc-one]))
  (testing "no conts type"
    (test-search-table :resources
                       " movie "
                       []))
  (testing "all conts requester-email"
    (test-search-table :resources
                       "l"
                       [test-rsrc-one test-rsrc-two test-rsrc-thr]))
  (testing "one cont requester-email"
    (test-search-table :resources
                       "what"
                       [test-rsrc-one]))
  (testing "no conts requester-email"
    (test-search-table :resources
                       "@gmail@com"
                       []))
  (testing "only a space"
    (test-search-table :resources
                       " "
                       [test-rsrc-one test-rsrc-two test-rsrc-thr])))

;; Need more tests here - this is bare minimum
(deftest test-search-contents
  (testing "all conts"
    (test-search-table :contents
                       "a"
                       [(update (update test-cont-one :resource-id str) :collection-id str)
                        (update (update test-cont-two :resource-id str) :collection-id str)
                        (update (update test-cont-thr :resource-id str) :collection-id str)]))
  (testing "one cont"
    (test-search-table :contents
                       "adfwe"
                       [(update (update test-cont-two :resource-id str) :collection-id str)]))
  (testing "no conts"
    (test-search-table :contents
                       "z"
                       [])))
