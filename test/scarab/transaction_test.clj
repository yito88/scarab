(ns scarab.transaction-test
  (:require [clojure.test :refer :all]
            [scarab.transaction :as t]))

; These tests are integration tests
; They need C* with keyspace "testks" and table "tx"

(deftest transaction-put-get-test
  (let [tx     (t/setup-transaction {})
        keys   [{:id {:value 1 :type :int}}]
        values {:val {:value 111 :type :int}}]
    (testing "Put and get a record"
             (t/start! tx)
             (t/put! tx "testks" "tx" keys values)
             (t/commit! tx)

             ; start a new transaction
             (t/start! tx)
             (let [r (t/get! tx "testks" "tx" keys)]
               (is (= (:id r)
                      {:value 1 :type :int}))
               (is (= (:val r)
                      {:value 111 :type :int}))))))
