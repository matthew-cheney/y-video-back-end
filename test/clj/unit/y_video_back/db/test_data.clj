(ns y-video-back.db.test-data
  "Entry database testing namespace, testing basic functions and providing functions for testing"
  (:require [y-video-back.db.test-util :as tu]
            [y-video-back.handler :refer[app]]
            [mount.core]))

;(tu/basic-transaction-fixtures
; (mount.core/start #'y-video-back.handler/app))
