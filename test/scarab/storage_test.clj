(ns scarab.storage-test
  (:require [clojure.test :refer :all]
            [scarab.storage :as st]))

; These tests are integration tests
; They need C* with keyspace "testks" and table "testtbl"

(deftest storage-put-get-test
  (let [storage (st/setup-storage {})
        keys    [{:id {:value 1 :type :int}}]
        values  {:val {:value 111 :type :int}}]
    (testing "Put and get a record"
             (st/put! storage "testks" "testtbl" keys values)
             (is (= (st/get! storage "testks" "testtbl" keys)
                    {:id {:value 1 :type :int}
                     :val {:value 111 :type :int}})))))
