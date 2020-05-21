(ns y-video-postgres-swagger.test.test_model_generator
  (:require
    [y-video-postgres-swagger.models :as models]))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn rand-bool []
  (if (< 0 (rand-int 2))
    true
    false))

(defn get_random_user_without_id
  "Generate a test user with random field values"
  [model]
  (into {} (map #(if (= (string? "") ((get % 1) ""))
                   [(get % 0) (rand-str 32)]
                   (if (= (boolean? true) ((get % 1) true))
                     [(get % 0) (rand-bool)]
                     [(get % 0) (rand-int 1000)]))
               model)))
